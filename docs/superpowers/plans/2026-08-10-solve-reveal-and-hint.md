# Solve Reveal + Hint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a 2s full-image reveal on solve and a limited-use (3/game) Hint that overlays the full photo for 2.5s while the timer keeps running.

**Architecture:** A pure `HintState` core owns the hint count (property-tested). `GameViewModel` holds the count and a single full-image `ImageBitmap` (loaded once per board via a new `ImageSlicer.loadFull` that reuses the slicer's center-crop). `BoardScreen` renders the Hint button, the UI-local timed overlay, suppresses tile borders when solved, and delays navigation to Complete by 2s.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Kotlin coroutines/StateFlow, Kotest (StringSpec + property `checkAll`).

## Global Constraints

- minSdk 29, JDK 21; build via `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew ...`.
- No `Co-Authored-By` / Claude reference in commit messages (Commitizen format).
- Property-Based Testing extension is enabled: pure decision logic MUST have Kotest property tests. UI glue is exempt.
- Boards are NOT persisted: hint count is in-memory, reset on new board / Restart.
- Constants: `REVEAL_HOLD_MS = 2000`, `HINT_MS = 2500`, `HINT_FADE_MS = 200` (in `BoardScreen.kt`); `HintState.MAX = 3`.
- Do not change swap/solve engine rules.

---

### Task 1: Pure `HintState` core + property tests

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/domain/model/HintState.kt`
- Test: `app/src/test/java/com/tessera/puzzle/domain/model/HintStatePropertiesTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `data class HintState(val remaining: Int)` with `val canUse: Boolean`, `fun use(): HintState`, and `companion object { const val MAX = 3; fun fresh(): HintState }`.

- [ ] **Step 1: Write the failing property tests**

`HintStatePropertiesTest.kt`:

```kotlin
package com.tessera.puzzle.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property tests for the pure hint-count core. Proves the count never goes
 * negative, decrements exactly when usable, and resets to MAX.
 */
class HintStatePropertiesTest : StringSpec({

    "fresh starts at MAX" {
        HintState.fresh().remaining shouldBe HintState.MAX
    }

    "use never drops below zero and canUse tracks remaining" {
        checkAll(Arb.int(0, 100)) { n ->
            val s = HintState(n)
            s.canUse shouldBe (n > 0)
            val next = s.use()
            (next.remaining >= 0) shouldBe true
        }
    }

    "use decrements by one when usable, else is identity" {
        checkAll(Arb.int(0, 100)) { n ->
            val s = HintState(n)
            val next = s.use()
            if (n > 0) next.remaining shouldBe n - 1
            else next.remaining shouldBe n
        }
    }

    "using MAX times reaches zero, further use stays zero" {
        var s = HintState.fresh()
        repeat(HintState.MAX) { s = s.use() }
        s.remaining shouldBe 0
        s.use().remaining shouldBe 0
    }
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.domain.model.HintStatePropertiesTest"`
Expected: FAIL (unresolved reference `HintState`).

- [ ] **Step 3: Write minimal implementation**

`HintState.kt`:

```kotlin
package com.tessera.puzzle.domain.model

/**
 * Pure hint-count core. Immutable; [use] decrements only while hints remain,
 * so the count never goes negative. No Android types — property-testable.
 */
data class HintState(val remaining: Int) {

    val canUse: Boolean get() = remaining > 0

    fun use(): HintState = if (canUse) HintState(remaining - 1) else this

    companion object {
        const val MAX = 3
        fun fresh(): HintState = HintState(MAX)
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.domain.model.HintStatePropertiesTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/domain/model/HintState.kt app/src/test/java/com/tessera/puzzle/domain/model/HintStatePropertiesTest.kt
git commit -m "feat(game): add pure HintState core with property tests"
```

---

### Task 2: `ImageSlicer.loadFull` — full center-cropped image

**Files:**
- Modify: `app/src/main/java/com/tessera/puzzle/data/ImageSlicer.kt`

**Interfaces:**
- Consumes: existing `computeInSampleSize`, `TARGET_EDGE_PX`, `sliceBitmap`'s center-crop math.
- Produces: `fun loadFull(context: Context, @DrawableRes imageRes: Int): ImageBitmap?` and `fun loadFull(imagePath: String): ImageBitmap?`, plus private `cropSquare(full: Bitmap): ImageBitmap`. Used by `GameViewModel` (Task 3).

**Note:** `sliceBitmap` currently computes the center crop inline (`side`, `left`, `top`, `Bitmap.createBitmap`). Refactor that crop into a shared private `cropSquareBitmap(full: Bitmap): Bitmap` so `sliceBitmap` and `loadFull` produce a pixel-identical square. This keeps the hint image aligned with the tiles.

- [ ] **Step 1: Refactor the center-crop out of `sliceBitmap`**

In `ImageSlicer.kt`, replace the crop lines inside `sliceBitmap`:

```kotlin
        val side = min(full.width, full.height)
        if (side < gridSize) return emptyList()
        val left = (full.width - side) / 2
        val top = (full.height - side) / 2
        val square = Bitmap.createBitmap(full, left, top, side, side)
```

with a call to a new shared helper:

```kotlin
        val square = cropSquareBitmap(full)
        val side = square.width
        if (side < gridSize) return emptyList()
```

And add the helper (place it near `computeInSampleSize`):

```kotlin
    /** Center-crop [full] to its largest centered square. */
    private fun cropSquareBitmap(full: Bitmap): Bitmap {
        val side = min(full.width, full.height)
        val left = (full.width - side) / 2
        val top = (full.height - side) / 2
        return Bitmap.createBitmap(full, left, top, side, side)
    }
```

- [ ] **Step 2: Add the two `loadFull` overloads**

Add to the `ImageSlicer` object (after the `slice(imagePath, ...)` overload):

```kotlin
    /** Load a bundled drawable as a single center-cropped square image. */
    fun loadFull(context: Context, @DrawableRes imageRes: Int): ImageBitmap? {
        val full = BitmapFactory.decodeResource(context.resources, imageRes)
            ?: return null
        return cropSquareBitmap(full).asImageBitmap()
    }

    /**
     * Load a saved image file as a single center-cropped square image. Bounded
     * decode (inSampleSize) matches [slice] so the result lines up with tiles.
     */
    fun loadFull(imagePath: String): ImageBitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val opts = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val full = BitmapFactory.decodeFile(imagePath, opts) ?: return null
        return cropSquareBitmap(full).asImageBitmap()
    }
```

- [ ] **Step 3: Compile to verify (no unit test — Android bitmap APIs need a device)**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. Also run the existing slicer tests to confirm the refactor didn't regress:
`./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.data.TileBoundsPropertiesTest"` → PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/data/ImageSlicer.kt
git commit -m "feat(data): add ImageSlicer.loadFull for the full center-cropped image"
```

---

### Task 3: Wire hints + full image into `GameViewModel`

**Files:**
- Modify: `app/src/main/java/com/tessera/puzzle/game/GameViewModel.kt`

**Interfaces:**
- Consumes: `HintState` (Task 1), `ImageSlicer.loadFull` (Task 2).
- Produces: `val hintsRemaining: StateFlow<Int>`, `val fullImage: StateFlow<ImageBitmap?>`, `fun useHint()`. Consumed by `BoardScreen` (Task 4).

- [ ] **Step 1: Add backing state fields**

After the existing `_complete` field (around line 61), add:

```kotlin
    private val _hints = MutableStateFlow(HintState.fresh())
    private val _fullImage = MutableStateFlow<ImageBitmap?>(null)
```

Add import: `import com.tessera.puzzle.domain.model.HintState`.

- [ ] **Step 2: Expose the public flows**

After the `completeUiState` declaration (around line 70), add:

```kotlin
    val hintsRemaining: StateFlow<Int> =
        _hints.map { it.remaining }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HintState.MAX)

    val fullImage: StateFlow<ImageBitmap?> = _fullImage.asStateFlow()
```

Add import: `import kotlinx.coroutines.flow.map`.

- [ ] **Step 3: Reset hints + clear full image in `startBoard`**

In `startBoard`, in the synchronous clear block (near `_complete.value = null`), add:

```kotlin
        _hints.value = HintState.fresh()
        _fullImage.value = null
```

- [ ] **Step 4: Load the full image in `loadTiles`**

In `loadTiles`, alongside slicing, load the full image on the same `default` dispatcher. Replace the `withContext(default) { ... }` block so it also returns the full image. Concretely, after computing `sliced`, add a parallel load:

```kotlin
    private fun loadTiles(puzzle: Puzzle, difficulty: Difficulty) {
        viewModelScope.launch {
            val (sliced, full) = withContext(default) {
                val path = puzzle.imagePath
                val tiles = if (path != null) {
                    ImageSlicer.slice(path, difficulty.gridSize)
                } else {
                    ImageSlicer.slice(app, puzzle.imageRes, difficulty.gridSize)
                }
                val fullImg = if (path != null) {
                    ImageSlicer.loadFull(path)
                } else {
                    ImageSlicer.loadFull(app, puzzle.imageRes)
                }
                tiles to fullImg
            }
            if (sliced.isEmpty()) {
                _error.value = "Couldn't load this puzzle's image — try another photo."
                _board.value = null
            } else {
                _tiles.value = sliced
                _fullImage.value = full
            }
        }
    }
```

- [ ] **Step 5: Reset hints in `restart`; clear full image in `exitBoard`; add `useHint`**

In `restart`, after building the fresh board, add `_hints.value = HintState.fresh()`.
In `exitBoard`, after `_tiles.value = emptyList()`, add `_fullImage.value = null`.
Add the method near `consumeComplete`:

```kotlin
    /** Consume one hint if any remain (no-op at 0). */
    fun useHint() {
        _hints.value = _hints.value.use()
    }
```

- [ ] **Step 6: Compile**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/game/GameViewModel.kt
git commit -m "feat(game): track hint count and full image in GameViewModel"
```

---

### Task 4: `PillButton` `enabled` support + Hint button, overlay, solve reveal in `BoardScreen`

**Files:**
- Modify: `app/src/main/java/com/tessera/puzzle/ui/theme/Primitives.kt`
- Modify: `app/src/main/java/com/tessera/puzzle/ui/screens/BoardScreen.kt`

**Interfaces:**
- Consumes: `game.hintsRemaining`, `game.fullImage`, `game.useHint()` (Task 3); `rememberReducedMotion()`.
- Produces: no downstream consumers (terminal UI task).

- [ ] **Step 1: Add `enabled` to `PillButton`**

In `Primitives.kt`, add an `enabled: Boolean = true` param to the main `PillButton` (the 5-param one at line 74) and the delegating overload at line 68-71. When disabled: skip the glow, drop opacity, and ignore clicks. Replace the body's modifier/click so disabled is inert:

```kotlin
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    foreground: Color = TesseraColors.Ink,
    enabled: Boolean = true,
) {
    val bg = if (filled) TesseraColors.Primary else TesseraColors.SurfaceAlt
    val fg = if (filled) TesseraColors.OnPrimary else foreground
    Row(
        modifier = modifier
            .then(if (filled && enabled) Modifier.primaryGlow(TesseraShapes.pill) else Modifier)
            .clip(TesseraShapes.pill)
            .background(bg)
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .heightIn(min = 48.dp)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = TesseraType.heading.copy(color = fg), textAlign = TextAlign.Center)
    }
}
```

Update the delegating overload (line ~68-71) to pass `enabled` through:

```kotlin
) = PillButton(text, onClick, modifier, filled, foreground, enabled)
```

and add `enabled: Boolean = true,` to that overload's parameter list too.

Add import at top of `Primitives.kt` if missing: `import androidx.compose.ui.graphics.graphicsLayer`.

- [ ] **Step 2: Compile the primitive change**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Collect hint state + full image in `BoardScreen`**

In `BoardScreen`, near the existing `state`/`board`/`tiles` collection (around line 77-81), add:

```kotlin
    val hintsRemaining by game.hintsRemaining.collectAsStateWithLifecycle()
    val fullImage by game.fullImage.collectAsStateWithLifecycle()
    val reducedMotion = rememberReducedMotion()
    var hintVisible by remember { mutableStateOf(false) }
    val hintAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
```

(`rememberReducedMotion`, `Animatable`, `rememberCoroutineScope`, `launch` are already imported or used in this file.)

- [ ] **Step 4: Delay navigation on solve (2s reveal hold)**

In the solve `LaunchedEffect` (lines 98-106), change the `onSolved()` call to hold first:

```kotlin
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
```

- [ ] **Step 5: Suppress tile borders when solved**

In `PuzzleBoard`, the per-tile border `when` block (lines 257-261) must draw nothing once solved so the image is seamless. Change it to:

```kotlin
                        when {
                            board.isSolved -> Modifier
                            selected -> Modifier.border(3.dp, TesseraColors.Primary)
                            canSwap -> Modifier.border(2.dp, TesseraColors.PrimaryLight)
                            else -> Modifier
                        },
```

- [ ] **Step 6: Add the Hint+Pause button Row and the hint overlay**

Replace the single `PillButton("Pause", ...)` (lines 167-172) with a Row of Hint + Pause spanning the board width:

```kotlin
        Row(
            Modifier.fillMaxWidth().widthIn(max = 560.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PillButton(
                text = "Hint (${hintsRemaining})",
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
```

Then, so the overlay draws on top of the whole board column, add a hint overlay Box AFTER the main `Column` closes (just before the `if (paused)` block, around line 174). It shows the full image over the board area:

```kotlin
    if (hintVisible && fullImage != null) {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
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
```

> Placement note: the overlay's `padding(20.dp)` + `TopCenter` + `widthIn(560.dp)` + `aspectRatio(1f)` mirrors the board column's own insets/padding so the hint image lands over the board (the header sits above it). If on-device testing shows a vertical offset, wrap the board + this overlay in a shared `Box` instead — but start with this simpler form.

- [ ] **Step 7: Add the new constants**

At the bottom of `BoardScreen.kt` with the other `private const val`s (near line 377-379), add:

```kotlin
private const val REVEAL_HOLD_MS = 2000
private const val HINT_MS = 2500
private const val HINT_FADE_MS = 200
```

Ensure `tween` is imported (it already is, line 6) and `Animatable` (line 4). Add `import androidx.compose.foundation.layout.WindowInsets` — already present (line 16).

- [ ] **Step 8: Build the debug APK + lint**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:assembleDebug :app:lintDebug`
Expected: BUILD SUCCESSFUL, 0 lint errors.

- [ ] **Step 9: Run the full unit/PBT suite**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest`
Expected: PASS (50 tests: prior 49 + HintState).

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/theme/Primitives.kt app/src/main/java/com/tessera/puzzle/ui/screens/BoardScreen.kt
git commit -m "feat(board): add hint overlay and full-image solve reveal"
```

---

## Self-Review

**Spec coverage:**
- Solve reveal (borders suppressed when solved + 2s hold before Complete) → Task 4 Steps 4-5. ✓
- Hint button below board beside Pause, `weight(1f)` Row, count label, disables at 0 → Task 4 Step 6 + `enabled` in Step 1. ✓
- Hint overlay full image 2.5s, fade, timer untouched (no `timerJob` access) → Task 4 Step 6. ✓
- 3 per game, reset on new board/Restart → Task 1 (`MAX=3`) + Task 3 Steps 3, 5. ✓
- Full image via `loadFull` reusing center-crop → Task 2. ✓
- Reduced motion → Task 4 Step 6 (snap vs fade). ✓
- Pure `HintState` + PBT → Task 1. ✓
- `PillButton` needs `enabled` (confirmed absent today) → Task 4 Step 1. ✓

**Placeholder scan:** No TBD/TODO; every code step has concrete code. The overlay placement note is a real fallback instruction, not a placeholder. ✓

**Type consistency:** `HintState.use`/`fresh`/`MAX`/`remaining` consistent across Tasks 1/3. `loadFull(context, imageRes)` / `loadFull(imagePath)` used exactly as defined (Task 2 → Task 3). `hintsRemaining: StateFlow<Int>`, `fullImage: StateFlow<ImageBitmap?>`, `useHint()` consistent Task 3 → Task 4. `enabled` param threaded through both `PillButton` overloads. ✓
