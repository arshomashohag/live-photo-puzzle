package com.tessera.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Direction
import com.tessera.puzzle.domain.model.Grid
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.PillButton
import com.tessera.puzzle.ui.theme.RoundedCard
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraShapes
import com.tessera.puzzle.ui.theme.TesseraType
import com.tessera.puzzle.ui.theme.clickableNoRipple
import com.tessera.puzzle.ui.theme.rememberReducedMotion
import kotlin.math.abs
import kotlinx.coroutines.launch

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}

/** A tile pair mid-animation: each slides from its previous cell into place. */
private data class TileAnim(val posA: Int, val posB: Int)

@Composable
fun BoardScreen(
    game: GameViewModel,
    puzzleId: String,
    difficulty: Difficulty,
    onSolved: () -> Unit,
    onExit: () -> Unit,
) {
    val state by game.boardUiState.collectAsStateWithLifecycle()
    val board = state.board
    val tiles = state.tiles
    val error = state.error
    var paused by remember { mutableStateOf(false) }

    val hintsRemaining by game.hintsRemaining.collectAsStateWithLifecycle()
    val fullImage by game.fullImage.collectAsStateWithLifecycle()
    val guideVisible by game.guideVisible.collectAsStateWithLifecycle()
    val reducedMotion = rememberReducedMotion()
    var hintVisible by remember { mutableStateOf(false) }
    val hintAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(puzzleId, difficulty) {
        val b = game.boardUiState.value.board
        if (b == null || b.puzzle.id != puzzleId || b.difficulty != difficulty || b.isSolved) {
            // Always start fresh here. If the previous board was already solved,
            // starting fresh reshuffles the SAME photo so it can be replayed —
            // and prevents the stale solved board from flashing the Complete
            // screen before the new scramble loads.
            game.startBoard(puzzleId, difficulty)
        }
    }

    // Navigate to Complete only after a solve that happens during THIS session:
    // require that the board went from unsolved → solved while mounted. A board
    // that is already solved on entry is ignored (startBoard reshuffles it).
    var wasUnsolved by remember(puzzleId, difficulty) { mutableStateOf(false) }
    LaunchedEffect(board?.isSolved, board?.puzzle?.id, board?.difficulty) {
        val b = board ?: return@LaunchedEffect
        if (b.puzzle.id != puzzleId || b.difficulty != difficulty) return@LaunchedEffect
        if (!b.isSolved) {
            wasUnsolved = true
        } else if (wasUnsolved) {
            kotlinx.coroutines.delay(REVEAL_HOLD_MS.toLong())
            onSolved()
        }
    }

    // Forced save on lifecycle stop (BR-2).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) game.flushSave()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = !paused) { paused = true }

    if (error != null) {
        BoardErrorOverlay(error, onExit = { game.consumeBoardError(); onExit() })
        return
    }

    if (board == null) {
        Box(Modifier.fillMaxSize().background(TesseraColors.Haze))
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.Haze)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxWidth().widthIn(max = 560.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "${board.puzzle.name.uppercase()} · ${difficulty.label.uppercase()}",
                    style = TesseraType.cardTitle,
                )
                Text(
                    "${board.placedCount}/${difficulty.tileCount} PLACED",
                    style = TesseraType.label.copy(color = TesseraColors.Muted),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatTime(board.elapsedMillis), style = TesseraType.mono.copy(color = TesseraColors.Ink))
                Text("${board.moves} MOVES", style = TesseraType.label.copy(color = TesseraColors.Muted))
            }
        }

        Box(
            Modifier.fillMaxWidth().widthIn(max = 560.dp),
            contentAlignment = Alignment.Center,
        ) {
            PuzzleBoard(
                game = game,
                board = board,
                tiles = tiles,
                difficulty = difficulty,
            )
            // Hint overlay: the full image drawn directly over the board. It
            // MUST use the exact same sizing chain as the LazyVerticalGrid in
            // PuzzleBoard (fillMaxWidth → widthIn(560) → aspectRatio(1) →
            // border) so both resolve to the identical width-driven square and
            // the overlay lands pixel-aligned on the tiles. Using fillMaxSize
            // here instead would resolve a different (height-driven) square and
            // make the image jump relative to the tiles. Timed/faded in the
            // Hint button's coroutine; the board state underneath is untouched.
            if (hintVisible && fullImage != null) {
                Image(
                    bitmap = fullImage!!,
                    contentDescription = "Hint: full image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .aspectRatio(1f)
                        .graphicsLayer { alpha = hintAlpha.value }
                        .border(1.dp, TesseraColors.Ink),
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().widthIn(max = 560.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PillButton(
                text = "Hint ($hintsRemaining)",
                onClick = {
                    if (hintVisible) return@PillButton
                    game.useHint()
                    hintVisible = true
                    scope.launch {
                        if (reducedMotion) {
                            hintAlpha.snapTo(1f)
                            kotlinx.coroutines.delay(HINT_MS.toLong())
                            hintAlpha.snapTo(0f)
                        } else {
                            hintAlpha.animateTo(1f, tween(HINT_FADE_MS))
                            kotlinx.coroutines.delay(HINT_MS.toLong())
                            hintAlpha.animateTo(0f, tween(HINT_FADE_MS))
                        }
                        hintVisible = false
                    }
                },
                modifier = Modifier.weight(1f),
                filled = false,
                enabled = hintsRemaining > 0 && !hintVisible,
            )
            PillButton(
                text = "Pause",
                onClick = { paused = true },
                modifier = Modifier.weight(1f),
                filled = false,
            )
        }
    }

    if (paused) {
        PauseOverlay(
            onResume = { paused = false },
            onRestart = { paused = false; game.restart() },
            onExit = { paused = false; game.exitBoard(); onExit() },
        )
    }

    if (guideVisible && !paused) {
        GuideOverlay(
            reducedMotion = reducedMotion,
            onDismiss = { game.dismissGuide() },
        )
    }
}

@Composable
private fun PuzzleBoard(
    game: GameViewModel,
    board: com.tessera.puzzle.domain.model.BoardState,
    tiles: List<androidx.compose.ui.graphics.ImageBitmap>,
    difficulty: Difficulty,
) {
    val gridSize = difficulty.gridSize
    val scope = rememberCoroutineScope()
    val reducedMotion = rememberReducedMotion()

    // Slide progress 1→0 for the two most-recently-swapped tiles.
    val slide = remember { Animatable(0f) }
    var anim by remember { mutableStateOf<TileAnim?>(null) }
    var animating by remember { mutableStateOf(false) }

    fun runSwipe(pos: Int, dir: Direction) {
        if (animating) return
        val target = Grid.neighborInDirection(pos, dir, gridSize) ?: return
        animating = true
        anim = TileAnim(pos, target)
        game.swipe(pos, dir)
        scope.launch {
            if (reducedMotion) {
                slide.snapTo(0f)
            } else {
                slide.snapTo(1f)
                slide.animateTo(0f, tween(SWAP_MS, easing = FastOutSlowInEasing))
            }
            anim = null
            animating = false
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .aspectRatio(1f)
            .border(1.dp, TesseraColors.Ink)
            .pointerInput(gridSize, board.order.size) {
                detectTileSwipes(gridSize) { pos, dir -> runSwipe(pos, dir) }
            },
        userScrollEnabled = false,
    ) {
        val sel = board.selected
        val swappable = if (sel != null) Grid.neighbors(sel, gridSize).toSet() else emptySet()
        items(count = board.order.size) { position ->
            val sourceIndex = board.order[position]
            val selected = board.selected == position
            val canSwap = position in swappable
            val placed = sourceIndex == position

            // Slide offset: an animating tile enters from the cell it swapped with.
            val a = anim
            val slideFrac = if (a != null && (position == a.posA || position == a.posB)) {
                val from = if (position == a.posA) a.posB else a.posA
                val dCol = ((from % gridSize) - (position % gridSize)).toFloat()
                val dRow = ((from / gridSize) - (position / gridSize)).toFloat()
                Pair(dCol, dRow)
            } else {
                null
            }

            Box(
                Modifier
                    .aspectRatio(1f)
                    .then(
                        // No neutral border: adjacent tiles would each draw an
                        // inset hairline, producing a visible ~1px seam between
                        // matching tiles. Only draw a border to highlight the
                        // selected / swappable tiles during play.
                        when {
                            board.isSolved -> Modifier
                            selected -> Modifier.border(3.dp, TesseraColors.Primary)
                            canSwap -> Modifier.border(2.dp, TesseraColors.PrimaryLight)
                            else -> Modifier
                        },
                    )
                    .semantics {
                        contentDescription = "Tile ${position + 1}" +
                            (if (selected) ", selected" else "") +
                            (if (canSwap) ", swappable" else "") +
                            (if (placed) ", in place" else "")
                    },
            ) {
                val tile = tiles.getOrNull(sourceIndex)
                val tileMod = if (slideFrac != null) {
                    Modifier.graphicsLayer {
                        translationX = slideFrac.first * size.width * slide.value
                        translationY = slideFrac.second * size.height * slide.value
                    }
                } else {
                    Modifier
                }
                if (tile != null) {
                    Image(
                        bitmap = tile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().then(tileMod),
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(TesseraColors.PrimaryLight))
                }
            }
        }
    }
}

/**
 * Detects a directional flick/drag on the board and reports the (tile position,
 * direction). Registers on release when total travel exceeds [SWIPE_THRESHOLD_DP]
 * along the dominant axis — one move per gesture; small jitters are ignored so
 * accidental taps don't move tiles.
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTileSwipes(
    gridSize: Int,
    onSwipe: (pos: Int, dir: Direction) -> Unit,
) {
    val thresholdPx = SWIPE_THRESHOLD_DP.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val cell = size.width.toFloat() / gridSize
        val col = (down.position.x / cell).toInt().coerceIn(0, gridSize - 1)
        val row = (down.position.y / cell).toInt().coerceIn(0, gridSize - 1)
        val pos = row * gridSize + col
        var dx = 0f
        var dy = 0f
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            dx += change.positionChange().x
            dy += change.positionChange().y
            if (!change.pressed) break
        }
        if (abs(dx) < thresholdPx && abs(dy) < thresholdPx) return@awaitEachGesture
        val dir = if (abs(dx) >= abs(dy)) {
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        } else {
            if (dy > 0) Direction.DOWN else Direction.UP
        }
        onSwipe(pos, dir)
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(SCRIM)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        RoundedCard(Modifier.padding(32.dp).widthIn(max = 360.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("PAUSED", style = TesseraType.display.copy(color = TesseraColors.Ink))
                PillButton("Resume", onResume, Modifier.fillMaxWidth())
                PillButton("Restart", onRestart, Modifier.fillMaxWidth(), filled = false)
                PillButton("Exit puzzle", onExit, Modifier.fillMaxWidth(), filled = false)
            }
        }
    }
}

/**
 * First-run coach-mark shown over the board the first time a puzzle is played.
 * Explains the swap gesture with a looping swipe indicator (paused when the
 * user prefers reduced motion). Dismissed by the button or by tapping the
 * scrim; [onDismiss] persists the "seen" flag so it never returns.
 */
@Composable
private fun GuideOverlay(reducedMotion: Boolean, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(SCRIM)
            .clickableNoRipple(onDismiss)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .semantics {
                contentDescription =
                    "How to play: swipe a tile toward its neighbour to swap them. " +
                        "Tap to dismiss."
            },
        contentAlignment = Alignment.Center,
    ) {
        RoundedCard(Modifier.padding(32.dp).widthIn(max = 360.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("SWIPE TO SWAP", style = TesseraType.display.copy(color = TesseraColors.Ink))
                SwipeHint(reducedMotion)
                Text(
                    "Swipe any tile up, down, left or right to swap it with the " +
                        "neighbour beside it. Line the photo back up to win.",
                    style = TesseraType.body.copy(color = TesseraColors.Muted),
                    textAlign = TextAlign.Center,
                )
                PillButton("Got it", onDismiss, Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * A small looping swipe cue: a tile that slides right and back, with a
 * double-headed arrow. Static (centered) under reduced motion.
 */
@Composable
private fun SwipeHint(reducedMotion: Boolean) {
    val shift = if (reducedMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "swipe")
        val v by transition.animateFloat(
            initialValue = 0f,
            targetValue = 26f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "swipeShift",
        )
        v
    }
    Row(
        Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .offset(x = shift.dp)
                .size(34.dp)
                .clip(TesseraShapes.card)
                .background(TesseraColors.Primary),
        )
        Text("⇄", style = TesseraType.display.copy(color = TesseraColors.Muted))
        Box(
            Modifier
                .size(34.dp)
                .clip(TesseraShapes.card)
                .background(TesseraColors.SurfaceAlt),
        )
    }
}

@Composable
private fun BoardErrorOverlay(message: String, onExit: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.Haze)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        RoundedCard(Modifier.padding(32.dp).widthIn(max = 360.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Can't start puzzle", style = TesseraType.heading.copy(color = TesseraColors.Ink))
                Text(message, style = TesseraType.body.copy(color = TesseraColors.Muted))
                PillButton("Go back", onExit, Modifier.fillMaxWidth())
            }
        }
    }
}

private const val SWAP_MS = 200
private const val SWIPE_THRESHOLD_DP = 24
private const val REVEAL_HOLD_MS = 2000
private const val HINT_MS = 2500
private const val HINT_FADE_MS = 200
private val SCRIM = androidx.compose.ui.graphics.Color(0xB3000000)
