package com.tessera.puzzle.game

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.puzzle.data.ImageSlicer
import com.tessera.puzzle.data.files.PuzzleFileStore
import com.tessera.puzzle.di.DefaultDispatcher
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Direction
import com.tessera.puzzle.domain.model.Puzzle
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import com.tessera.puzzle.domain.repository.PuzzleRepository
import com.tessera.puzzle.domain.repository.StatsRepository
import com.tessera.puzzle.presentation.BoardUiState
import com.tessera.puzzle.presentation.CompleteUiState
import com.tessera.puzzle.presentation.HomeUiState
import com.tessera.puzzle.presentation.PuzzleListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orchestrates the pure engine. Exposes StateFlow UI state (UDF). In-progress
 * boards are intentionally NOT persisted — each entry to a puzzle starts a fresh
 * game; only completion stats are recorded. Pause is in-session UI only.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val app: Application,
    private val puzzleRepository: PuzzleRepository,
    private val statsRepository: StatsRepository,
    private val fileStore: PuzzleFileStore,
    @IoDispatcher private val io: CoroutineDispatcher,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : AndroidViewModel(app) {

    private val _board = MutableStateFlow<BoardState?>(null)
    private val _tiles = MutableStateFlow<List<ImageBitmap>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)
    private val _restoreNotice = MutableStateFlow(false)
    private val _complete = MutableStateFlow<CompleteUiState?>(null)

    private var timerJob: Job? = null

    val boardUiState: StateFlow<BoardUiState> =
        combine(_board, _tiles, _error) { board, tiles, error ->
            BoardUiState(board, tiles, error)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardUiState())

    val completeUiState: StateFlow<CompleteUiState?> = _complete.asStateFlow()

    // In-progress boards are not persisted: no Continue card, no resume.
    val homeUiState: StateFlow<HomeUiState> =
        statsRepository.observeHomeStats()
            .let { flow ->
                combine(flow, _restoreNotice) { stats, notice ->
                    HomeUiState(stats = stats, continueInfo = null, restoreNotice = notice)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

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
    }

    /**
     * Resolve a PuzzleRecord to a playable engine Puzzle. Drawable-backed
     * (bundled) and file-backed (custom photos) are both supported. Returns null
     * if the image is unavailable (missing drawable / missing custom file), so
     * the caller can surface a recoverable error rather than a blank board.
     */
    private suspend fun toEnginePuzzle(record: PuzzleRecord): Puzzle? = withContext(io) {
        when (val ref = record.imageRef) {
            is ImageRef.DrawableRef -> {
                val resId = app.resources.getIdentifier(ref.resName, "drawable", app.packageName)
                if (resId == 0) null else Puzzle(record.id, record.name, imageRes = resId)
            }
            is ImageRef.FileRef ->
                if (!fileStore.filesExist(ref)) null
                else Puzzle(record.id, record.name, imagePath = ref.imagePath)
        }
    }

    fun startBoard(puzzleId: String, difficulty: Difficulty) {
        viewModelScope.launch {
            _error.value = null
            _complete.value = null // clear any stale completion from a previous puzzle
            val record = puzzleRepository.getPuzzle(puzzleId)
            if (record == null) {
                _error.value = "This puzzle is no longer available."
                return@launch
            }
            puzzleNameCache[record.id] = record.name
            val enginePuzzle = toEnginePuzzle(record)
            if (enginePuzzle == null) {
                _error.value = "Couldn't load this puzzle's image — the photo may be missing."
                return@launch
            }
            // Always start a fresh scramble — in-progress boards are not saved
            // or resumed. Each entry to a puzzle begins a new game.
            val board = BoardState.new(enginePuzzle, difficulty)
            _board.value = board
            _tiles.value = emptyList()
            loadTiles(enginePuzzle, difficulty)
            startTimer()
        }
    }

    private fun loadTiles(puzzle: Puzzle, difficulty: Difficulty) {
        viewModelScope.launch {
            val sliced = withContext(default) {
                val path = puzzle.imagePath
                if (path != null) {
                    ImageSlicer.slice(path, difficulty.gridSize)
                } else {
                    ImageSlicer.slice(app, puzzle.imageRes, difficulty.gridSize)
                }
            }
            if (sliced.isEmpty()) {
                _error.value = "Couldn't load this puzzle's image — try another photo."
                _board.value = null
            } else {
                _tiles.value = sliced
            }
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
        }
    }

    /**
     * Swipe [pos] toward [direction], swapping with its edge-adjacent neighbor.
     * A swipe toward a board edge (no neighbor) is a no-op. Same engine rules as
     * [tap]; progress is not persisted.
     */
    fun swipe(pos: Int, direction: Direction) {
        val b = _board.value ?: return
        if (b.isSolved) return
        val next = b.swipe(pos, direction)
        if (next === b) return
        _board.value = next
        if (next.isSolved) {
            timerJob?.cancel()
            onSolved(next)
        }
    }

    fun consumeBoardError() { _error.value = null }

    private fun onSolved(board: BoardState) {
        viewModelScope.launch {
            statsRepository.recordCompletion(
                board.puzzle.id, board.difficulty, board.elapsedMillis, board.moves,
            )
            _complete.value = CompleteUiState(
                puzzleName = board.puzzle.name,
                difficulty = board.difficulty,
                elapsedMillis = board.elapsedMillis,
                moves = board.moves,
                best = statsRepository.bestScore(board.puzzle.id, board.difficulty),
            )
        }
    }

    /** No-op: in-progress boards are not persisted. Kept for lifecycle callers. */
    fun flushSave() = Unit

    fun restart() {
        val current = _board.value ?: return
        _board.value = BoardState.new(current.puzzle, current.difficulty)
        startTimer()
    }

    fun exitBoard() {
        timerJob?.cancel()
        _board.value = null
        _tiles.value = emptyList()
        _error.value = null
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
}
