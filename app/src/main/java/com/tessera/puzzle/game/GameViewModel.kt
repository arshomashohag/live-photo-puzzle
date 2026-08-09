package com.tessera.puzzle.game

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.puzzle.data.ImageSlicer
import com.tessera.puzzle.data.PuzzleCatalog
import com.tessera.puzzle.model.BoardState
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.model.Puzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CompletedRun(
    val puzzle: Puzzle,
    val difficulty: Difficulty,
    val elapsedMillis: Long,
    val moves: Int,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val _board = mutableStateOf<BoardState?>(null)
    val board: State<BoardState?> = _board

    private val _tiles = mutableStateOf<List<ImageBitmap>>(emptyList())
    val tiles: State<List<ImageBitmap>> = _tiles

    private val _lastCompleted = mutableStateOf<CompletedRun?>(null)
    val lastCompleted: State<CompletedRun?> = _lastCompleted

    private var timerJob: Job? = null

    val hasBoardInProgress: Boolean
        get() = _board.value?.let { !it.isSolved } ?: false

    fun startBoard(puzzleId: String, difficulty: Difficulty) {
        val puzzle = PuzzleCatalog.byId(puzzleId) ?: return
        beginBoard(puzzle, difficulty)
    }

    fun restart() {
        val current = _board.value ?: return
        beginBoard(current.puzzle, current.difficulty)
    }

    private fun beginBoard(puzzle: Puzzle, difficulty: Difficulty) {
        _board.value = BoardState.new(puzzle, difficulty)
        _tiles.value = emptyList()
        viewModelScope.launch {
            val sliced = withContext(Dispatchers.Default) {
                ImageSlicer.slice(getApplication(), puzzle.imageRes, difficulty.gridSize)
            }
            _tiles.value = sliced
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val start = System.currentTimeMillis()
            while (isActive) {
                val b = _board.value ?: break
                if (b.isSolved) break
                _board.value = b.withElapsed(System.currentTimeMillis() - start)
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
            _lastCompleted.value =
                CompletedRun(next.puzzle, next.difficulty, next.elapsedMillis, next.moves)
        }
    }

    fun exitBoard() {
        timerJob?.cancel()
        _board.value = null
        _tiles.value = emptyList()
    }
}
