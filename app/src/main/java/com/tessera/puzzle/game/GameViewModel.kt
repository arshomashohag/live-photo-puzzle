package com.tessera.puzzle.game

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.puzzle.data.ImageSlicer
import com.tessera.puzzle.di.DefaultDispatcher
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Puzzle
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import com.tessera.puzzle.domain.model.persistence.SavedBoard
import com.tessera.puzzle.domain.repository.BoardRepository
import com.tessera.puzzle.domain.repository.PuzzleRepository
import com.tessera.puzzle.domain.repository.StatsRepository
import com.tessera.puzzle.presentation.BoardUiState
import com.tessera.puzzle.presentation.CompleteUiState
import com.tessera.puzzle.presentation.ContinueInfo
import com.tessera.puzzle.presentation.HomeUiState
import com.tessera.puzzle.presentation.PuzzleListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orchestrates the pure engine with durable persistence. Exposes StateFlow UI
 * state (UDF). Autosave is debounced (~750 ms, coalescing) with forced saves on
 * lifecycle-critical moments (BR-2).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class GameViewModel @Inject constructor(
    private val app: Application,
    private val puzzleRepository: PuzzleRepository,
    private val boardRepository: BoardRepository,
    private val statsRepository: StatsRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : AndroidViewModel(app) {

    private val _board = MutableStateFlow<BoardState?>(null)
    private val _tiles = MutableStateFlow<List<ImageBitmap>>(emptyList())
    private val _restoreNotice = MutableStateFlow(false)
    private val _complete = MutableStateFlow<CompleteUiState?>(null)

    /** Emits board snapshots to be debounced-saved. */
    private val saveRequests = MutableSharedFlow<BoardState>(extraBufferCapacity = 64)

    private var timerJob: Job? = null

    val boardUiState: StateFlow<BoardUiState> =
        combine(_board, _tiles) { board, tiles -> BoardUiState(board, tiles) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardUiState())

    val completeUiState: StateFlow<CompleteUiState?> = _complete.asStateFlow()

    val homeUiState: StateFlow<HomeUiState> =
        combine(
            statsRepository.observeHomeStats(),
            boardRepository.observeMostRecent(),
            _restoreNotice,
        ) { stats, recent, notice ->
            HomeUiState(
                stats = stats,
                continueInfo = recent?.let {
                    ContinueInfo(
                        puzzleId = it.puzzleId,
                        puzzleName = puzzleNameCache[it.puzzleId] ?: it.puzzleId,
                        difficulty = it.difficulty,
                        placed = it.order.indices.count { i -> it.order[i] == i },
                        total = it.difficulty.tileCount,
                    )
                },
                restoreNotice = notice,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val puzzleNameCache = mutableMapOf<String, String>()

    val puzzles: StateFlow<List<PuzzleListItem>> =
        puzzleRepository.observePuzzles()
            .onEach { list -> list.forEach { puzzleNameCache[it.id] = it.name } }
            .let { flow ->
                combine(flow, _board) { list, _ -> list.map { PuzzleListItem(it) } }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { puzzleRepository.ensureSeeded() }
        // Debounced autosave pipeline (BR-2): coalesce rapid moves.
        saveRequests
            .debounce(DEBOUNCE_MS)
            .onEach { board -> persist(board) }
            .launchIn(viewModelScope)
    }

    /** Resolve a PuzzleRecord to a playable engine Puzzle (drawable-backed only in P1). */
    private suspend fun toEnginePuzzle(record: PuzzleRecord): Puzzle? = withContext(io) {
        when (val ref = record.imageRef) {
            is ImageRef.DrawableRef -> {
                val resId = app.resources.getIdentifier(ref.resName, "drawable", app.packageName)
                if (resId == 0) null else Puzzle(record.id, record.name, resId)
            }
            is ImageRef.FileRef -> null // Phase 2
        }
    }

    fun startBoard(puzzleId: String, difficulty: Difficulty) {
        viewModelScope.launch {
            val record = puzzleRepository.getPuzzle(puzzleId) ?: return@launch
            puzzleNameCache[record.id] = record.name
            val enginePuzzle = toEnginePuzzle(record) ?: return@launch
            val existing = boardRepository.loadBoard(puzzleId, difficulty)
            val board = if (existing != null) {
                BoardState(
                    puzzle = enginePuzzle,
                    difficulty = difficulty,
                    order = existing.order,
                    selected = existing.selected,
                    moves = existing.moves,
                    elapsedMillis = existing.elapsedMillis,
                )
            } else {
                BoardState.new(enginePuzzle, difficulty)
            }
            _board.value = board
            _tiles.value = emptyList()
            loadTiles(enginePuzzle, difficulty)
            if (existing == null) persist(board) // forced save so new board is recoverable
            startTimer()
        }
    }

    private fun loadTiles(puzzle: Puzzle, difficulty: Difficulty) {
        viewModelScope.launch {
            val sliced = withContext(default) {
                ImageSlicer.slice(app, puzzle.imageRes, difficulty.gridSize)
            }
            _tiles.value = sliced
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startBase = System.currentTimeMillis() - (_board.value?.elapsedMillis ?: 0L)
            while (isActive) {
                val b = _board.value ?: break
                if (b.isSolved) break
                _board.value = b.withElapsed(System.currentTimeMillis() - startBase)
                delay(250)
            }
        }
    }

    fun tap(pos: Int) {
        val b = _board.value ?: return
        if (b.isSolved) return
        val next = b.tapTile(pos)
        _board.value = next
        if (next.isSolved) {
            timerJob?.cancel()
            onSolved(next)
        } else {
            saveRequests.tryEmit(next) // debounced
        }
    }

    private fun onSolved(board: BoardState) {
        viewModelScope.launch {
            statsRepository.recordCompletion(
                board.puzzle.id, board.difficulty, board.elapsedMillis, board.moves,
            )
            boardRepository.clearBoard(board.puzzle.id, board.difficulty)
            _complete.value = CompleteUiState(
                puzzleName = board.puzzle.name,
                difficulty = board.difficulty,
                elapsedMillis = board.elapsedMillis,
                moves = board.moves,
                best = statsRepository.bestScore(board.puzzle.id, board.difficulty),
            )
        }
    }

    /** Forced immediate save (onStop / Pause). */
    fun flushSave() {
        val b = _board.value ?: return
        if (!b.isSolved) viewModelScope.launch { persist(b) }
    }

    private suspend fun persist(board: BoardState) {
        if (board.isSolved) return
        boardRepository.saveBoard(
            SavedBoard(
                puzzleId = board.puzzle.id,
                difficulty = board.difficulty,
                order = board.order,
                selected = board.selected,
                moves = board.moves,
                elapsedMillis = board.elapsedMillis,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun restart() {
        val current = _board.value ?: return
        val puzzle = current.puzzle
        val difficulty = current.difficulty
        viewModelScope.launch {
            boardRepository.clearBoard(puzzle.id, difficulty)
            val fresh = BoardState.new(puzzle, difficulty)
            _board.value = fresh
            persist(fresh)
            startTimer()
        }
    }

    fun exitBoard() {
        timerJob?.cancel()
        flushSave()
        _board.value = null
        _tiles.value = emptyList()
    }

    fun consumeRestoreNotice() {
        _restoreNotice.value = false
    }

    fun consumeComplete() {
        _complete.value = null
    }

    /** Delete a custom puzzle (row + files); bundled puzzles are rejected in the repo. */
    fun deleteCustomPuzzle(puzzleId: String) {
        viewModelScope.launch { puzzleRepository.deletePuzzle(puzzleId) }
    }

    private companion object {
        const val DEBOUNCE_MS = 750L
    }
}
