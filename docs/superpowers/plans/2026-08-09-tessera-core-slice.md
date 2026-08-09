# Tessera Core Playable Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an installable, fully-offline Android photo-puzzle game (Tessera) with a swap-tile mechanic: launch → pick difficulty → pick puzzle → solve by swapping tiles → win.

**Architecture:** Single-Activity Jetpack Compose app. Navigation-Compose routes between 7 screens. Pure Kotlin domain layer (`Difficulty`, `Puzzle`, `BoardState`, scramble) is fully unit-tested on the JVM with no Android dependencies. Bundled JPEGs in `res/drawable-nodpi/` are decoded and sliced into N×N tile bitmaps at runtime — no network, no `INTERNET` permission. A shared `GameViewModel` holds in-memory game/session state.

**Tech Stack:** Kotlin, Jetpack Compose (Material3, re-themed), Navigation-Compose, AndroidX Lifecycle ViewModel-Compose, Gradle Kotlin DSL + version catalog, JUnit4 for unit tests.

## Global Constraints

- Package: `com.tessera.puzzle`
- minSdk = 26; compileSdk = 35; targetSdk = 35
- Language: Kotlin only. UI: Jetpack Compose only (no XML layouts except manifest/themes/values).
- **No `INTERNET` permission** in the manifest. App must run with airplane mode on.
- Light theme only for this slice. No dark-theme resources.
- Design tokens (copy verbatim): ink `#1D1F20`, paper `#F2F2F3`, haze `#E7E7EA`, steel `#5980A6`, steelDeep `#2C455D`, sky `#94BCE3`, mist `#D6EBFF`, muted `#5D5D60`, faint `#7A7A7D`; hairline = `#1D1F20` at 28% alpha.
- Difficulties: EASY = 3×3 (9 tiles), MEDIUM = 4×4 (16), HARD = 5×5 (25).
- Swap-tile mechanic: tap tile A, tap tile B → the two swap. Every permutation is solvable (no parity constraint). A board is solved when tile at position `i` has source index `i` for all `i`.
- Display type: Barlow Condensed (headings, UPPERCASE). Body: Barlow. Labels: monospace (system monospace acceptable).
- Fonts Barlow + Barlow Condensed are OFL — bundle `.ttf` under `res/font/`.
- Commit after every task. Commit messages: plain, imperative, no AI/co-author trailer.
- Screens must lay out within `WindowInsets.safeDrawing`; do not draw a fake status bar.

---

## File Structure

```
settings.gradle.kts                      # root settings, repositories
build.gradle.kts                         # root, plugin versions via catalog
gradle.properties                        # AndroidX/Jetifier/JVM flags
gradle/libs.versions.toml                # version catalog
gradle/wrapper/gradle-wrapper.properties # gradle distribution pin
gradlew, gradlew.bat                     # wrapper scripts
tools/fetch_puzzle_images.sh             # dev-time image fetch (reproducible)

app/build.gradle.kts                     # android app module
app/src/main/AndroidManifest.xml         # single activity, NO internet perm
app/src/main/java/com/tessera/puzzle/
  MainActivity.kt                        # ComponentActivity + setContent + splash
  TesseraApp.kt                          # NavHost, route constants
  model/
    Difficulty.kt                        # enum
    Puzzle.kt                            # data class
    Scramble.kt                          # pure scramble fn (seedable)
    BoardState.kt                        # immutable board + tapTile/solved
  data/
    PuzzleCatalog.kt                     # 9 bundled puzzles, 3 per difficulty
    ImageSlicer.kt                       # bitmap decode + center-crop + slice
  game/
    GameViewModel.kt                     # in-memory session/board state
  ui/theme/
    Color.kt                             # tokens
    Type.kt                              # Barlow families
    Theme.kt                             # TesseraTheme wrapper
    Primitives.kt                        # RegistrationFrame, BlueprintButton, DifficultyMeter, GridPreview
  ui/screens/
    SplashScreen.kt
    HomeScreen.kt
    DifficultyScreen.kt
    PuzzleSelectScreen.kt
    BoardScreen.kt                       # board + Pause overlay
    CompleteScreen.kt
app/src/main/res/
  drawable-nodpi/                        # 9 committed JPEGs
  font/                                  # barlow*.ttf
  values/ (themes.xml, strings.xml, colors.xml)
  mipmap-*/ (launcher icon)
app/src/test/java/com/tessera/puzzle/
  model/ScrambleTest.kt
  model/BoardStateTest.kt
```

---

### Task 1: Gradle project skeleton that builds

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/values/themes.xml`, `app/src/main/res/values/strings.xml`, `app/src/main/java/com/tessera/puzzle/MainActivity.kt`

**Interfaces:**
- Produces: a buildable `:app` module; `MainActivity` (empty `setContent {}`), package `com.tessera.puzzle`.

- [ ] **Step 1: Generate the Gradle wrapper**

If Android Studio / a local Gradle is available, run `gradle wrapper --gradle-version 8.9`. Otherwise create `gradle/wrapper/gradle-wrapper.properties` with:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

and fetch the wrapper jar + `gradlew` scripts (copy from any AGP 8.x project, or `gradle wrapper`).

- [ ] **Step 2: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.6.1"
kotlin = "2.0.20"
composeBom = "2024.09.03"
coreKtx = "1.13.1"
lifecycle = "2.8.6"
activityCompose = "1.9.2"
navigationCompose = "2.8.1"
splashscreen = "1.0.1"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "splashscreen" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 3: Write `settings.gradle.kts` and root `build.gradle.kts`**

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Tessera"
include(":app")
```

Root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

`gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Write `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.tessera.puzzle"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tessera.puzzle"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
```

- [ ] **Step 5: Write manifest, theme, strings, MainActivity**

`app/src/main/AndroidManifest.xml` (note: NO `<uses-permission android:name="android.permission.INTERNET"/>`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.Tessera.Starting"
        android:supportsRtl="true">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Tessera.Starting">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

`app/src/main/res/values/themes.xml`:

```xml
<resources>
    <style name="Theme.Tessera" parent="android:Theme.Material.Light.NoActionBar" />
    <style name="Theme.Tessera.Starting" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">#1D2D3D</item>
        <item name="postSplashScreenTheme">@style/Theme.Tessera</item>
    </style>
</resources>
```

`app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">Tessera</string>
</resources>
```

`MainActivity.kt`:

```kotlin
package com.tessera.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { }
    }
}
```

- [ ] **Step 6: Verify it builds**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. If the wrapper jar is missing, resolve Step 1 first.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/ gradlew gradlew.bat app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res app/src/main/java
git commit -m "Scaffold Tessera Android app module"
```

---

### Task 2: Difficulty and Puzzle domain types

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/model/Difficulty.kt`, `app/src/main/java/com/tessera/puzzle/model/Puzzle.kt`

**Interfaces:**
- Produces: `enum class Difficulty(val gridSize: Int) { EASY(3), MEDIUM(4), HARD(5) }` with `val tileCount: Int get() = gridSize * gridSize` and `val label: String` (EASY→"Easy", etc.); `data class Puzzle(val id: String, val name: String, val imageRes: Int)`.

- [ ] **Step 1: Write `Difficulty.kt`**

```kotlin
package com.tessera.puzzle.model

enum class Difficulty(val gridSize: Int) {
    EASY(3),
    MEDIUM(4),
    HARD(5);

    val tileCount: Int get() = gridSize * gridSize
    val label: String get() = name.lowercase().replaceFirstChar { it.uppercase() }
    val level: Int get() = ordinal + 1
}
```

- [ ] **Step 2: Write `Puzzle.kt`**

```kotlin
package com.tessera.puzzle.model

import androidx.annotation.DrawableRes

data class Puzzle(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
)
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/model/Difficulty.kt app/src/main/java/com/tessera/puzzle/model/Puzzle.kt
git commit -m "Add Difficulty and Puzzle domain types"
```

---

### Task 3: Scramble generation (TDD)

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/model/Scramble.kt`
- Test: `app/src/test/java/com/tessera/puzzle/model/ScrambleTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `fun scramble(tileCount: Int, random: kotlin.random.Random = Random.Default): IntArray` — returns a permutation of `0 until tileCount`; never the identity; every index appears exactly once.

- [ ] **Step 1: Write the failing test**

`ScrambleTest.kt`:

```kotlin
package com.tessera.puzzle.model

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrambleTest {

    @Test
    fun scramble_isValidPermutation() {
        val n = 16
        val result = scramble(n, Random(1))
        assertEquals(n, result.size)
        assertEquals((0 until n).toList(), result.sorted())
    }

    @Test
    fun scramble_isNotIdentity() {
        repeat(50) { seed ->
            val n = 9
            val result = scramble(n, Random(seed.toLong()))
            val identity = (0 until n).any { result[it] != it }
            assertTrue("scramble must differ from solved", identity)
        }
    }

    @Test
    fun scramble_isDeterministicForSeed() {
        val a = scramble(25, Random(42))
        val b = scramble(25, Random(42))
        assertTrue(a.contentEquals(b))
    }

    @Test
    fun scramble_smallestBoardNeverIdentity() {
        assertFalse(scramble(9, Random(0)).contentEquals(IntArray(9) { it }))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.model.ScrambleTest"`
Expected: FAIL (unresolved reference `scramble`).

- [ ] **Step 3: Write minimal implementation**

`Scramble.kt`:

```kotlin
package com.tessera.puzzle.model

import kotlin.random.Random

fun scramble(tileCount: Int, random: Random = Random.Default): IntArray {
    require(tileCount >= 2) { "tileCount must be >= 2" }
    val order = IntArray(tileCount) { it }
    do {
        for (i in tileCount - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = order[i]
            order[i] = order[j]
            order[j] = tmp
        }
    } while (order.withIndex().all { (i, v) -> i == v })
    return order
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.model.ScrambleTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/model/Scramble.kt app/src/test/java/com/tessera/puzzle/model/ScrambleTest.kt
git commit -m "Add seedable board scramble with tests"
```

---

### Task 4: BoardState — swap, placed-count, solved (TDD)

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/model/BoardState.kt`
- Test: `app/src/test/java/com/tessera/puzzle/model/BoardStateTest.kt`

**Interfaces:**
- Consumes: `Difficulty`, `Puzzle`, `scramble(...)`.
- Produces:
  - `class BoardState(val puzzle: Puzzle, val difficulty: Difficulty, val order: IntArray, val selected: Int?, val moves: Int, val elapsedMillis: Long)`
  - `fun tapTile(pos: Int): BoardState`
  - `val placedCount: Int`, `val isSolved: Boolean`
  - `fun withElapsed(ms: Long): BoardState`
  - companion `fun BoardState.Companion.new(puzzle, difficulty, random): BoardState` producing a scrambled, unselected, zero-move board.

- [ ] **Step 1: Write the failing test**

`BoardStateTest.kt`:

```kotlin
package com.tessera.puzzle.model

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardStateTest {

    private val puzzle = Puzzle("p1", "Ridgeline", 0)

    private fun solvedOrder(n: Int) = IntArray(n) { it }

    private fun board(order: IntArray) = BoardState(
        puzzle = puzzle,
        difficulty = Difficulty.EASY,
        order = order,
        selected = null,
        moves = 0,
        elapsedMillis = 0L,
    )

    @Test
    fun new_isScrambledAndUnsolved() {
        val b = BoardState.new(puzzle, Difficulty.EASY, Random(3))
        assertEquals(9, b.order.size)
        assertFalse(b.isSolved)
        assertNull(b.selected)
        assertEquals(0, b.moves)
    }

    @Test
    fun tapTile_selectsThenSwaps() {
        // order [1,0,2,3,4,5,6,7,8] -> swapping pos0 and pos1 solves the first two
        val start = board(intArrayOf(1, 0, 2, 3, 4, 5, 6, 7, 8))
        val afterFirstTap = start.tapTile(0)
        assertEquals(0, afterFirstTap.selected)
        assertEquals(0, afterFirstTap.moves)

        val afterSwap = afterFirstTap.tapTile(1)
        assertNull(afterSwap.selected)
        assertEquals(1, afterSwap.moves)
        assertTrue(afterSwap.isSolved)
    }

    @Test
    fun tapTile_sameTileDeselects() {
        val start = board(intArrayOf(1, 0, 2, 3, 4, 5, 6, 7, 8))
        val selected = start.tapTile(0)
        val deselected = selected.tapTile(0)
        assertNull(deselected.selected)
        assertEquals(0, deselected.moves)
    }

    @Test
    fun placedCount_countsCorrectTiles() {
        val b = board(intArrayOf(0, 1, 2, 3, 4, 5, 6, 8, 7))
        assertEquals(7, b.placedCount)
        assertFalse(b.isSolved)
    }

    @Test
    fun isSolved_trueForIdentity() {
        assertTrue(board(solvedOrder(9)).isSolved)
        assertEquals(9, board(solvedOrder(9)).placedCount)
    }

    @Test
    fun withElapsed_updatesTimeOnly() {
        val b = board(solvedOrder(9)).withElapsed(1234L)
        assertEquals(1234L, b.elapsedMillis)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.model.BoardStateTest"`
Expected: FAIL (unresolved reference `BoardState`).

- [ ] **Step 3: Write minimal implementation**

`BoardState.kt`:

```kotlin
package com.tessera.puzzle.model

import kotlin.random.Random

class BoardState(
    val puzzle: Puzzle,
    val difficulty: Difficulty,
    val order: IntArray,
    val selected: Int?,
    val moves: Int,
    val elapsedMillis: Long,
) {
    val placedCount: Int
        get() = order.indices.count { order[it] == it }

    val isSolved: Boolean
        get() = order.indices.all { order[it] == it }

    fun tapTile(pos: Int): BoardState {
        val current = selected
        return when (current) {
            null -> copy(selected = pos)
            pos -> copy(selected = null)
            else -> {
                val next = order.copyOf()
                val tmp = next[current]
                next[current] = next[pos]
                next[pos] = tmp
                copy(order = next, selected = null, moves = moves + 1)
            }
        }
    }

    fun withElapsed(ms: Long): BoardState = copy(elapsedMillis = ms)

    private fun copy(
        order: IntArray = this.order,
        selected: Int? = this.selected,
        moves: Int = this.moves,
        elapsedMillis: Long = this.elapsedMillis,
    ) = BoardState(puzzle, difficulty, order, selected, moves, elapsedMillis)

    companion object {
        fun new(puzzle: Puzzle, difficulty: Difficulty, random: Random = Random.Default): BoardState =
            BoardState(
                puzzle = puzzle,
                difficulty = difficulty,
                order = scramble(difficulty.tileCount, random),
                selected = null,
                moves = 0,
                elapsedMillis = 0L,
            )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.model.BoardStateTest"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/model/BoardState.kt app/src/test/java/com/tessera/puzzle/model/BoardStateTest.kt
git commit -m "Add BoardState with swap, placed-count, and solved logic"
```

---

### Task 5: Fetch and bundle puzzle images

**Files:**
- Create: `tools/fetch_puzzle_images.sh`
- Create (binary, generated by the script): `app/src/main/res/drawable-nodpi/tessera_*.jpg` (9 files)

**Interfaces:**
- Produces: 9 committed JPEG drawables named `tessera_easy_1.jpg` … `tessera_hard_3.jpg`, ≤ ~180 KB each.

- [ ] **Step 1: Write the fetch script**

`tools/fetch_puzzle_images.sh` — deterministic seeds so the set is reproducible; requires `curl` and `sips` (macOS) or `convert` (ImageMagick) for resize/compress. Fetch at build/dev time only; the app never touches the network.

```bash
#!/usr/bin/env bash
set -euo pipefail
OUT="app/src/main/res/drawable-nodpi"
mkdir -p "$OUT"

# name:seed pairs — seeds pinned for reproducibility
IMAGES=(
  "tessera_easy_1:ridgeline"
  "tessera_easy_2:harbour"
  "tessera_easy_3:terrace"
  "tessera_medium_1:meridian"
  "tessera_medium_2:lattice"
  "tessera_medium_3:quartz"
  "tessera_hard_1:cordon"
  "tessera_hard_2:bastion"
  "tessera_hard_3:cascade"
)

resize() { # $1 = file
  if command -v sips >/dev/null 2>&1; then
    sips -Z 1024 "$1" >/dev/null
    sips -s formatOptions 80 "$1" >/dev/null
  elif command -v convert >/dev/null 2>&1; then
    convert "$1" -resize 1024x1024^ -gravity center -extent 1024x1024 -quality 80 "$1"
  fi
}

for pair in "${IMAGES[@]}"; do
  name="${pair%%:*}"; seed="${pair##*:}"
  echo "Fetching $name (seed=$seed)"
  curl -sL "https://picsum.photos/seed/${seed}/1024/1024.jpg" -o "$OUT/${name}.jpg"
  resize "$OUT/${name}.jpg"
done
echo "Done. Files in $OUT"
```

- [ ] **Step 2: Run the script**

Run: `chmod +x tools/fetch_puzzle_images.sh && ./tools/fetch_puzzle_images.sh`
Expected: 9 JPEGs in `app/src/main/res/drawable-nodpi/`. Verify sizes: `ls -la app/src/main/res/drawable-nodpi/`. Each should be well under 200 KB. If any exceeds it, re-run resize or lower quality to 70.

- [ ] **Step 3: Verify drawables are valid resource names**

Resource file names must be lowercase, digits, underscore only. Confirm: `ls app/src/main/res/drawable-nodpi/ | grep -vE '^tessera_(easy|medium|hard)_[123]\.jpg$'` prints nothing.

- [ ] **Step 4: Commit**

```bash
git add tools/fetch_puzzle_images.sh app/src/main/res/drawable-nodpi/
git commit -m "Bundle royalty-free puzzle photos and fetch script"
```

---

### Task 6: PuzzleCatalog

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/data/PuzzleCatalog.kt`

**Interfaces:**
- Consumes: `Puzzle`, `Difficulty`, generated `R.drawable.tessera_*`.
- Produces: `object PuzzleCatalog { fun forDifficulty(d: Difficulty): List<Puzzle>; fun byId(id: String): Puzzle?; val all: List<Puzzle> }`. Puzzle ids: `easy-1`, `easy-2`, `easy-3`, `medium-1`, …, `hard-3`.

- [ ] **Step 1: Write `PuzzleCatalog.kt`**

```kotlin
package com.tessera.puzzle.data

import com.tessera.puzzle.R
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.model.Puzzle

object PuzzleCatalog {

    private val easy = listOf(
        Puzzle("easy-1", "Ridgeline", R.drawable.tessera_easy_1),
        Puzzle("easy-2", "Harbour", R.drawable.tessera_easy_2),
        Puzzle("easy-3", "Terrace", R.drawable.tessera_easy_3),
    )
    private val medium = listOf(
        Puzzle("medium-1", "Meridian", R.drawable.tessera_medium_1),
        Puzzle("medium-2", "Lattice", R.drawable.tessera_medium_2),
        Puzzle("medium-3", "Quartz", R.drawable.tessera_medium_3),
    )
    private val hard = listOf(
        Puzzle("hard-1", "Cordon", R.drawable.tessera_hard_1),
        Puzzle("hard-2", "Bastion", R.drawable.tessera_hard_2),
        Puzzle("hard-3", "Cascade", R.drawable.tessera_hard_3),
    )

    val all: List<Puzzle> = easy + medium + hard

    fun forDifficulty(d: Difficulty): List<Puzzle> = when (d) {
        Difficulty.EASY -> easy
        Difficulty.MEDIUM -> medium
        Difficulty.HARD -> hard
    }

    fun byId(id: String): Puzzle? = all.firstOrNull { it.id == id }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (R.drawable ids resolve because Task 5 committed the drawables).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/data/PuzzleCatalog.kt
git commit -m "Add bundled puzzle catalog"
```

---

### Task 7: ImageSlicer

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/data/ImageSlicer.kt`

**Interfaces:**
- Consumes: Android `Context`, `@DrawableRes Int`, grid size.
- Produces: `object ImageSlicer { fun slice(context: Context, imageRes: Int, gridSize: Int): List<ImageBitmap> }` returning `gridSize*gridSize` tiles in row-major order (index `r*gridSize + c`). Decodes the drawable, center-crops to a square, divides into equal cells. Runs on a background dispatcher when called (caller uses `Dispatchers.Default`).

- [ ] **Step 1: Write `ImageSlicer.kt`**

```kotlin
package com.tessera.puzzle.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.min

object ImageSlicer {

    fun slice(context: Context, @DrawableRes imageRes: Int, gridSize: Int): List<ImageBitmap> {
        val full = BitmapFactory.decodeResource(context.resources, imageRes)
            ?: return emptyList()
        val side = min(full.width, full.height)
        val left = (full.width - side) / 2
        val top = (full.height - side) / 2
        val square = Bitmap.createBitmap(full, left, top, side, side)
        val cell = side / gridSize

        val tiles = ArrayList<ImageBitmap>(gridSize * gridSize)
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val tile = Bitmap.createBitmap(square, c * cell, r * cell, cell, cell)
                tiles.add(tile.asImageBitmap())
            }
        }
        return tiles
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/data/ImageSlicer.kt
git commit -m "Add image slicer for tile bitmaps"
```

---

### Task 8: Theme — colors, type, TesseraTheme

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/theme/Color.kt`, `Type.kt`, `Theme.kt`
- Create: `app/src/main/res/font/` — `barlow_regular.ttf`, `barlow_medium.ttf`, `barlow_semibold.ttf`, `barlow_condensed_semibold.ttf`, `barlow_condensed_bold.ttf` (download from Google Fonts, OFL)

**Interfaces:**
- Produces: `object TesseraColors` with named `Color` tokens; `val Barlow: FontFamily`, `val BarlowCondensed: FontFamily`, `object TesseraType` with `display`, `heading`, `body`, `label`, `mono` `TextStyle`s; `@Composable fun TesseraTheme(content: @Composable () -> Unit)`.

- [ ] **Step 1: Add fonts**

Download Barlow + Barlow Condensed TTFs from fonts.google.com (OFL). Rename to the lowercase names above and place in `app/src/main/res/font/`. If download is unavailable, `Type.kt` may fall back to `FontFamily.SansSerif` for Barlow and `FontFamily.SansSerif` (condensed unavailable) — but prefer bundling.

- [ ] **Step 2: Write `Color.kt`**

```kotlin
package com.tessera.puzzle.ui.theme

import androidx.compose.ui.graphics.Color

object TesseraColors {
    val Ink = Color(0xFF1D1F20)
    val Paper = Color(0xFFF2F2F3)
    val Haze = Color(0xFFE7E7EA)
    val Steel = Color(0xFF5980A6)
    val SteelDeep = Color(0xFF2C455D)
    val Sky = Color(0xFF94BCE3)
    val Mist = Color(0xFFD6EBFF)
    val Muted = Color(0xFF5D5D60)
    val Faint = Color(0xFF7A7A7D)
    val Hairline = Color(0x471D1F20) // #1D1F20 @ 28%
    val SplashBg = Color(0xFF1D2D3D)
}
```

- [ ] **Step 3: Write `Type.kt`**

```kotlin
package com.tessera.puzzle.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.sp
import com.tessera.puzzle.R

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
)

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

object TesseraType {
    val display = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, letterSpacing = 2.sp)
    val heading = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = 1.sp)
    val cardTitle = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
    val body = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Normal, fontSize = 14.sp)
    val label = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 2.sp)
    val mono = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 12.sp)
}
```

If fonts were not bundled, replace the two `FontFamily(...)` blocks with `FontFamily.SansSerif` and delete the `R.font` imports.

- [ ] **Step 4: Write `Theme.kt`**

```kotlin
package com.tessera.puzzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TesseraColorScheme = lightColorScheme(
    primary = TesseraColors.Steel,
    onPrimary = TesseraColors.Paper,
    background = TesseraColors.Paper,
    onBackground = TesseraColors.Ink,
    surface = TesseraColors.Paper,
    onSurface = TesseraColors.Ink,
)

@Composable
fun TesseraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TesseraColorScheme, content = content)
}
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/theme app/src/main/res/font
git commit -m "Add blueprint theme: colors, type, fonts"
```

---

### Task 9: Blueprint primitives

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/theme/Primitives.kt`

**Interfaces:**
- Consumes: `TesseraColors`, `TesseraType`.
- Produces composables:
  - `RegistrationFrame(modifier, cornerColor = Steel, borderColor = Hairline, content)` — box with a hairline border and monospace `+` marks at each corner.
  - `BlueprintButton(text, onClick, modifier, filled: Boolean = true, leadingIcon: (@Composable () -> Unit)? = null)`.
  - `DifficultyMeter(level: Int, modifier)` — 3 bars, first `level` filled with `Steel`.
  - `GridPreview(gridSize: Int, modifier)` — an N×N line grid over a duotone background.

- [ ] **Step 1: Write `Primitives.kt`**

```kotlin
package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RegistrationFrame(
    modifier: Modifier = Modifier,
    cornerColor: Color = TesseraColors.Steel,
    borderColor: Color = TesseraColors.Hairline,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor, RectangleShape),
        )
        content()
        CornerMark(cornerColor, Alignment.TopStart)
        CornerMark(cornerColor, Alignment.TopEnd)
        CornerMark(cornerColor, Alignment.BottomStart)
        CornerMark(cornerColor, Alignment.BottomEnd)
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.CornerMark(color: Color, alignment: Alignment) {
    Text(
        text = "+",
        style = TesseraType.mono.copy(color = color),
        modifier = Modifier
            .align(alignment)
            .padding(0.dp)
    )
}

@Composable
fun BlueprintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val bg = if (filled) TesseraColors.Steel else Color.Transparent
    val fg = if (filled) TesseraColors.Paper else TesseraColors.Ink
    Row(
        modifier = modifier
            .background(bg)
            .then(if (filled) Modifier else Modifier.border(1.dp, TesseraColors.Hairline))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickableNoRipple(onClick),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
        }
        Text(text.uppercase(), style = TesseraType.heading.copy(color = fg), textAlign = TextAlign.Center)
    }
}

@Composable
fun DifficultyMeter(level: Int, modifier: Modifier = Modifier) {
    Row(modifier) {
        repeat(3) { i ->
            Box(
                Modifier
                    .padding(end = 3.dp)
                    .width(22.dp)
                    .height(4.dp)
                    .background(if (i < level) TesseraColors.Steel else TesseraColors.Hairline)
            )
        }
    }
}

@Composable
fun GridPreview(gridSize: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(TesseraColors.Sky)
            .drawBehind {
                val step = size.width / gridSize
                val line = 1.dp.toPx()
                val c = Color(0xB3F2F2F3)
                for (i in 1 until gridSize) {
                    drawRect(c, topLeft = androidx.compose.ui.geometry.Offset(i * step, 0f), size = androidx.compose.ui.geometry.Size(line, size.height))
                    drawRect(c, topLeft = androidx.compose.ui.geometry.Offset(0f, i * step), size = androidx.compose.ui.geometry.Size(size.width, line))
                }
            }
    )
}
```

- [ ] **Step 2: Add the no-ripple click helper**

Append to `Primitives.kt`:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.composed

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/theme/Primitives.kt
git commit -m "Add blueprint UI primitives"
```

---

### Task 10: GameViewModel — session state

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/game/GameViewModel.kt`

**Interfaces:**
- Consumes: `BoardState`, `Puzzle`, `Difficulty`, `PuzzleCatalog`, `ImageSlicer`, Android `Application`.
- Produces:
  - `class GameViewModel(app: Application) : AndroidViewModel(app)`
  - `val board: State<BoardState?>` (current board, or null)
  - `val tiles: State<List<ImageBitmap>>` (sliced source tiles for the current puzzle, empty until loaded)
  - `val lastCompleted: State<CompletedRun?>` where `data class CompletedRun(val puzzle: Puzzle, val difficulty: Difficulty, val elapsedMillis: Long, val moves: Int)`
  - `fun startBoard(puzzleId: String, difficulty: Difficulty)` — creates a fresh scrambled board, launches tile slicing on `Dispatchers.Default`, starts the timer.
  - `fun tap(pos: Int)` — delegates to `board.tapTile`; on becoming solved, stops the timer and sets `lastCompleted`.
  - `fun restart()` — reshuffle the current puzzle/difficulty.
  - `fun exitBoard()` — clear the current board (keeps `lastCompleted`).
  - `val hasBoardInProgress: Boolean` — true if `board != null && !board.isSolved`.

- [ ] **Step 1: Write `GameViewModel.kt`**

```kotlin
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
            _lastCompleted.value = CompletedRun(next.puzzle, next.difficulty, next.elapsedMillis, next.moves)
        }
    }

    fun exitBoard() {
        timerJob?.cancel()
        _board.value = null
        _tiles.value = emptyList()
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/game/GameViewModel.kt
git commit -m "Add GameViewModel for in-memory session state"
```

---

### Task 11: Navigation host + route constants

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/TesseraApp.kt`
- Modify: `app/src/main/java/com/tessera/puzzle/MainActivity.kt`

**Interfaces:**
- Consumes: `TesseraTheme`, `GameViewModel`, all screen composables (added in later tasks — provide temporary placeholders now, replace as each screen task lands).
- Produces: `object Routes { const val SPLASH; HOME; DIFFICULTY; puzzleSelect(d); board(puzzleId, d); COMPLETE }` with builder helpers, and `@Composable fun TesseraApp()` hosting the `NavHost` with a shared `GameViewModel` (activity-scoped via `viewModel()`).

- [ ] **Step 1: Write `TesseraApp.kt` with routes and placeholder destinations**

```kotlin
package com.tessera.puzzle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraTheme

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val DIFFICULTY = "difficulty"
    const val PUZZLE_SELECT = "puzzleSelect/{difficulty}"
    const val BOARD = "board/{puzzleId}/{difficulty}"
    const val COMPLETE = "complete"

    fun puzzleSelect(d: Difficulty) = "puzzleSelect/${d.name}"
    fun board(puzzleId: String, d: Difficulty) = "board/$puzzleId/${d.name}"
}

@Composable
fun TesseraApp() {
    TesseraTheme {
        Surface(color = TesseraColors.Haze, modifier = Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            val game: GameViewModel = viewModel()
            NavHost(navController = nav, startDestination = Routes.SPLASH) {
                composable(Routes.SPLASH) { Text("splash") }
                composable(Routes.HOME) { Text("home") }
                composable(Routes.DIFFICULTY) { Text("difficulty") }
                composable(
                    Routes.PUZZLE_SELECT,
                    arguments = listOf(navArgument("difficulty") { type = NavType.StringType }),
                ) { Text("select") }
                composable(
                    Routes.BOARD,
                    arguments = listOf(
                        navArgument("puzzleId") { type = NavType.StringType },
                        navArgument("difficulty") { type = NavType.StringType },
                    ),
                ) { Text("board") }
                composable(Routes.COMPLETE) { Text("complete") }
            }
        }
    }
}
```

- [ ] **Step 2: Wire `MainActivity` to `TesseraApp`**

Replace `setContent { }` body:

```kotlin
        setContent { TesseraApp() }
```

Add import `import com.tessera.puzzle.TesseraApp` is unnecessary (same package). Ensure `TesseraApp` is referenced.

- [ ] **Step 3: Verify build + install**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/TesseraApp.kt app/src/main/java/com/tessera/puzzle/MainActivity.kt
git commit -m "Add navigation host and route constants"
```

---

### Task 12: Splash screen

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/SplashScreen.kt`
- Modify: `TesseraApp.kt` (replace splash placeholder + auto-advance to Home)

**Interfaces:**
- Consumes: `TesseraColors`, `TesseraType`.
- Produces: `@Composable fun SplashScreen(onDone: () -> Unit)` — dark `SplashBg` canvas, centered animated 3×3 icon + "TESSERA / PHOTO PUZZLE"; calls `onDone()` after ≤800ms via `LaunchedEffect`.

- [ ] **Step 1: Write `SplashScreen.kt`**

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible.value) 1f else 0f, tween(400), label = "splash")

    LaunchedEffect(Unit) {
        visible.value = true
        delay(750)
        onDone()
    }

    Box(
        Modifier.fillMaxSize().background(TesseraColors.SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.alpha(alpha).size(96.dp).background(TesseraColors.Paper), contentAlignment = Alignment.Center) {
                Text("▦", style = TesseraType.display.copy(color = TesseraColors.SplashBg))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TESSERA", style = TesseraType.display.copy(color = TesseraColors.Paper, fontSize = 46.sp), textAlign = TextAlign.Center)
                Text("PHOTO PUZZLE", style = TesseraType.label.copy(color = TesseraColors.Sky), modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
```

- [ ] **Step 2: Wire into nav**

In `TesseraApp.kt`, replace the splash composable body:

```kotlin
                composable(Routes.SPLASH) {
                    com.tessera.puzzle.ui.screens.SplashScreen(onDone = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    })
                }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/SplashScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add splash screen"
```

---

### Task 13: Home screen

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/HomeScreen.kt`
- Modify: `TesseraApp.kt` (wire Home)

**Interfaces:**
- Consumes: `GameViewModel`, `RegistrationFrame`, `BlueprintButton`, `DifficultyMeter`, `GridPreview`, `Difficulty`, `TesseraType`, `TesseraColors`.
- Produces: `@Composable fun HomeScreen(game: GameViewModel, onContinue: () -> Unit, onPickDifficulty: (Difficulty) -> Unit)`. Shows: header (logo + wordmark), Continue card (only if `game.hasBoardInProgress`), Create-from-camera CTA (solid steel; tap shows inline "Coming soon"), difficulty grid (3 cards → `onPickDifficulty`), a My-puzzles stub row (non-interactive), stats strip (static numbers).

- [ ] **Step 1: Write `HomeScreen.kt`**

Full implementation — header, optional continue card, camera CTA with a `coming` toggle, difficulty grid mapping over `Difficulty.values()`, stats strip. Uses `Column` with `verticalScroll`, padded by `WindowInsets.safeDrawing`.

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.ui.theme.BlueprintButton
import com.tessera.puzzle.ui.theme.DifficultyMeter
import com.tessera.puzzle.ui.theme.GridPreview
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun HomeScreen(
    game: GameViewModel,
    onContinue: () -> Unit,
    onPickDifficulty: (Difficulty) -> Unit,
) {
    var coming by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val board = game.board.value

    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(TesseraColors.Steel))
            Spacer(Modifier.width(12.dp))
            Text("TESSERA", style = TesseraType.heading.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified, color = TesseraColors.Ink))
        }

        if (board != null && !board.isSolved) {
            Text("CONTINUE", style = TesseraType.label.copy(color = TesseraColors.Faint))
            RegistrationFrame(Modifier.fillMaxWidth().height(110.dp).clickable { onContinue() }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.Center) {
                    Text("${board.puzzle.name} · ${board.difficulty.label}", style = TesseraType.cardTitle)
                    Text("${board.difficulty.gridSize}×${board.difficulty.gridSize} · ${board.placedCount}/${board.difficulty.tileCount} placed", style = TesseraType.body.copy(color = TesseraColors.Muted))
                }
            }
        }

        Box(
            Modifier.fillMaxWidth().height(60.dp).background(TesseraColors.Steel).clickable { coming = true },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (coming) "COMING SOON" else "CREATE FROM CAMERA", style = TesseraType.heading.copy(color = TesseraColors.Paper))
        }

        Text("CHOOSE A DIFFICULTY", style = TesseraType.label.copy(color = TesseraColors.Faint))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Difficulty.values().forEach { d ->
                RegistrationFrame(
                    Modifier.weight(1f).height(150.dp).clickable { onPickDifficulty(d) },
                ) {
                    Column {
                        GridPreview(d.gridSize, Modifier.fillMaxWidth().height(64.dp))
                        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(d.label.uppercase(), style = TesseraType.cardTitle)
                            Text("${d.gridSize} × ${d.gridSize} · ${d.tileCount} tiles", style = TesseraType.body.copy(color = TesseraColors.Muted))
                            DifficultyMeter(d.level)
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().height(64.dp).background(TesseraColors.Paper)) {
            StatCell("SOLVED", "18", Modifier.weight(1f))
            StatCell("BEST 3×3", "00:41", Modifier.weight(1f))
            StatCell("CREATED", "7", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(12.dp)) {
        Text(label, style = TesseraType.label.copy(color = TesseraColors.Faint))
        Spacer(Modifier.height(6.dp))
        Text(value, style = TesseraType.cardTitle)
    }
}
```

Note on `RegistrationFrame` weight/height + `clickable`: place `.clickable` on the frame modifier as shown. The `Row` items use `Modifier.weight(1f)` inside a `RowScope`, which is valid.

- [ ] **Step 2: Wire Home into nav**

In `TesseraApp.kt`, replace the home composable:

```kotlin
                composable(Routes.HOME) {
                    com.tessera.puzzle.ui.screens.HomeScreen(
                        game = game,
                        onContinue = {
                            val b = game.board.value ?: return@HomeScreen
                            nav.navigate(Routes.board(b.puzzle.id, b.difficulty))
                        },
                        onPickDifficulty = { d -> nav.navigate(Routes.puzzleSelect(d)) },
                    )
                }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. Fix any weight/scope compile errors before committing.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/HomeScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add home screen"
```

---

### Task 14: Difficulty screen

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/DifficultyScreen.kt`
- Modify: `TesseraApp.kt`

**Interfaces:**
- Consumes: `Difficulty`, primitives, `TesseraType`, `TesseraColors`.
- Produces: `@Composable fun DifficultyScreen(onBack: () -> Unit, onPick: (Difficulty) -> Unit)` — top bar with back arrow + "CHOOSE DIFFICULTY", three large rows (grid preview + name + level + meter + count line).

- [ ] **Step 1: Write `DifficultyScreen.kt`**

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.ui.theme.DifficultyMeter
import com.tessera.puzzle.ui.theme.GridPreview
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun DifficultyScreen(onBack: () -> Unit, onPick: (Difficulty) -> Unit) {
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                tint = TesseraColors.Ink,
                modifier = Modifier.size(48.dp).padding(12.dp).clickable { onBack() },
            )
            Spacer(Modifier.width(6.dp))
            Text("CHOOSE DIFFICULTY", style = TesseraType.heading)
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Difficulty.values().forEach { d ->
                RegistrationFrame(Modifier.fillMaxWidth().height(140.dp).clickable { onPick(d) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        GridPreview(d.gridSize, Modifier.size(112.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(d.label.uppercase(), style = TesseraType.heading.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified))
                                Spacer(Modifier.width(8.dp))
                                Text("LEVEL ${d.level}/3", style = TesseraType.label.copy(color = TesseraColors.Faint))
                            }
                            Text("${d.gridSize} × ${d.gridSize} grid · ${d.tileCount} tiles", style = TesseraType.body)
                            DifficultyMeter(d.level)
                        }
                    }
                }
            }
        }
    }
}
```

Note: `material-icons-extended` is not added; `Icons.AutoMirrored.Filled.ArrowBack` is in the core `material-icons-core` bundled with material3. If it does not resolve, add `implementation("androidx.compose.material:material-icons-core")` to `app/build.gradle.kts` (it is part of the Compose BOM) or replace the icon with a `Text("←")` using `TesseraType.heading`.

- [ ] **Step 2: Wire into nav**

```kotlin
                composable(Routes.DIFFICULTY) {
                    com.tessera.puzzle.ui.screens.DifficultyScreen(
                        onBack = { nav.popBackStack() },
                        onPick = { d -> nav.navigate(Routes.puzzleSelect(d)) },
                    )
                }
```

(The Difficulty destination is reachable as a standalone route; Home currently routes straight to PuzzleSelect. Keep the DIFFICULTY route wired for completeness and future entry points.)

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. If the arrow icon fails to resolve, apply the fallback in the note.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/DifficultyScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add difficulty screen"
```

---

### Task 15: Puzzle select screen

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/PuzzleSelectScreen.kt`
- Modify: `TesseraApp.kt`

**Interfaces:**
- Consumes: `PuzzleCatalog`, `Difficulty`, `ImageSlicer` (for thumbnails, optional — a solid duotone thumbnail is acceptable), primitives.
- Produces: `@Composable fun PuzzleSelectScreen(difficulty: Difficulty, onBack: () -> Unit, onPick: (Puzzle) -> Unit)` — top bar "{Difficulty} · N×N", a 2-column grid of the 3 puzzles; each cell shows the puzzle image (decoded from `imageRes`), name, grid size.

- [ ] **Step 1: Write `PuzzleSelectScreen.kt`**

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.data.PuzzleCatalog
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.model.Puzzle
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun PuzzleSelectScreen(difficulty: Difficulty, onBack: () -> Unit, onPick: (Puzzle) -> Unit) {
    val puzzles = PuzzleCatalog.forDifficulty(difficulty)
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                tint = TesseraColors.Ink,
                modifier = Modifier.size(48.dp).padding(12.dp).clickable { onBack() },
            )
            Spacer(Modifier.width(6.dp))
            Text("${difficulty.label.uppercase()} · ${difficulty.gridSize} × ${difficulty.gridSize}", style = TesseraType.heading)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(puzzles, key = { it.id }) { p ->
                RegistrationFrame(Modifier.fillMaxWidth().clickable { onPick(p) }) {
                    Column {
                        Image(
                            painter = painterResource(p.imageRes),
                            contentDescription = p.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                        Column(Modifier.padding(10.dp)) {
                            Text(p.name.uppercase(), style = TesseraType.cardTitle)
                            Text("${difficulty.gridSize} × ${difficulty.gridSize}", style = TesseraType.body.copy(color = TesseraColors.Muted))
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Wire into nav (parse difficulty arg)**

```kotlin
                composable(
                    Routes.PUZZLE_SELECT,
                    arguments = listOf(navArgument("difficulty") { type = NavType.StringType }),
                ) { entry ->
                    val d = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
                    com.tessera.puzzle.ui.screens.PuzzleSelectScreen(
                        difficulty = d,
                        onBack = { nav.popBackStack() },
                        onPick = { p -> nav.navigate(Routes.board(p.id, d)) },
                    )
                }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/PuzzleSelectScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add puzzle select screen"
```

---

### Task 16: Board screen + Pause overlay (the gameplay)

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/BoardScreen.kt`
- Modify: `TesseraApp.kt`

**Interfaces:**
- Consumes: `GameViewModel`, `BoardState`, `ImageBitmap` tiles, primitives, `Difficulty`.
- Produces: `@Composable fun BoardScreen(game: GameViewModel, puzzleId: String, difficulty: Difficulty, onSolved: () -> Unit, onExit: () -> Unit)`. On first composition, calls `game.startBoard` if the current board doesn't match. Renders: header (name + timer + moves), the N×N grid of tappable tiles (selected tile gets a steel border), and a Pause button. System back opens the Pause overlay (`BackHandler`). Pause overlay: Resume / Restart / Exit. Watches `board.isSolved` → `onSolved()`.

- [ ] **Step 1: Write `BoardScreen.kt`**

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.ui.theme.BlueprintButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}

@Composable
fun BoardScreen(
    game: GameViewModel,
    puzzleId: String,
    difficulty: Difficulty,
    onSolved: () -> Unit,
    onExit: () -> Unit,
) {
    LaunchedEffect(puzzleId, difficulty) {
        val b = game.board.value
        if (b == null || b.puzzle.id != puzzleId || b.difficulty != difficulty || b.isSolved) {
            game.startBoard(puzzleId, difficulty)
        }
    }

    val board = game.board.value
    val tiles = game.tiles.value
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(board?.isSolved) {
        if (board != null && board.isSolved) onSolved()
    }

    BackHandler(enabled = !paused) { paused = true }

    if (board == null) {
        Box(Modifier.fillMaxSize().background(TesseraColors.Haze))
        return
    }

    Column(
        Modifier.fillMaxSize().background(TesseraColors.Haze).windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${board.puzzle.name.uppercase()} · ${difficulty.label.uppercase()}", style = TesseraType.cardTitle)
                Text("${board.placedCount}/${difficulty.tileCount} PLACED", style = TesseraType.label.copy(color = TesseraColors.Faint))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatTime(board.elapsedMillis), style = TesseraType.mono.copy(color = TesseraColors.Ink))
                Text("${board.moves} MOVES", style = TesseraType.label.copy(color = TesseraColors.Faint))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(difficulty.gridSize),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).border(1.dp, TesseraColors.Ink),
            userScrollEnabled = false,
        ) {
            itemsIndexed(
                items = (0 until board.order.size).toList(),
            ) { position, _ ->
                val sourceIndex = board.order[position]
                val selected = board.selected == position
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clickable { game.tap(position) }
                        .then(if (selected) Modifier.border(3.dp, TesseraColors.Steel) else Modifier.border(0.5.dp, TesseraColors.Hairline)),
                ) {
                    val tile = tiles.getOrNull(sourceIndex)
                    if (tile != null) {
                        Image(bitmap = tile, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize().background(TesseraColors.Sky))
                    }
                }
            }
        }

        Spacer(Modifier.height(0.dp))
        BlueprintButton(text = "Pause", onClick = { paused = true }, modifier = Modifier.fillMaxWidth(), filled = false)
    }

    if (paused) {
        PauseOverlay(
            onResume = { paused = false },
            onRestart = { paused = false; game.restart() },
            onExit = { paused = false; game.exitBoard(); onExit() },
        )
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(TesseraColors.SplashBg.copy(alpha = 0.92f)).windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("PAUSED", style = TesseraType.display.copy(color = TesseraColors.Paper))
            BlueprintButton("Resume", onResume, Modifier.fillMaxWidth())
            BlueprintButton("Restart", onRestart, Modifier.fillMaxWidth(), filled = false)
            BlueprintButton("Exit puzzle", onExit, Modifier.fillMaxWidth(), filled = false)
        }
    }
}
```

Note: the `BlueprintButton` outlined variant renders paper-on-dark poorly on the pause overlay (ink text on dark). For the pause overlay, pass a light foreground by wrapping labels — acceptable for the slice to leave as-is, or override `BlueprintButton` to accept a `foreground` color. If legibility is bad in manual testing, add a `foreground: Color = TesseraColors.Ink` param to `BlueprintButton` and pass `TesseraColors.Paper` here.

- [ ] **Step 2: Wire into nav**

```kotlin
                composable(
                    Routes.BOARD,
                    arguments = listOf(
                        navArgument("puzzleId") { type = NavType.StringType },
                        navArgument("difficulty") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val pid = entry.arguments!!.getString("puzzleId")!!
                    val d = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
                    com.tessera.puzzle.ui.screens.BoardScreen(
                        game = game,
                        puzzleId = pid,
                        difficulty = d,
                        onSolved = {
                            nav.navigate(Routes.COMPLETE) {
                                popUpTo(Routes.BOARD) { inclusive = true }
                            }
                        },
                        onExit = { nav.popBackStack() },
                    )
                }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/BoardScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add board screen with swap gameplay and pause overlay"
```

---

### Task 17: Complete screen

**Files:**
- Create: `app/src/main/java/com/tessera/puzzle/ui/screens/CompleteScreen.kt`
- Modify: `TesseraApp.kt`

**Interfaces:**
- Consumes: `GameViewModel.lastCompleted` (`CompletedRun`), primitives.
- Produces: `@Composable fun CompleteScreen(game: GameViewModel, onNext: () -> Unit, onHome: () -> Unit)` — full-screen dark win state showing puzzle name, time, moves, plus "Next puzzle" and "Home" actions. `BackHandler` routes to `onHome`-equivalent (puzzle list) — here we send back to Home for the slice.

- [ ] **Step 1: Write `CompleteScreen.kt`**

```kotlin
package com.tessera.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.BlueprintButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

@Composable
fun CompleteScreen(game: GameViewModel, onNext: () -> Unit, onHome: () -> Unit) {
    BackHandler { onHome() }
    val run = game.lastCompleted.value

    Box(
        Modifier.fillMaxSize().background(TesseraColors.SplashBg).windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("SOLVED", style = TesseraType.display.copy(color = TesseraColors.Paper))
            if (run != null) {
                Text(run.puzzle.name.uppercase(), style = TesseraType.heading.copy(color = TesseraColors.Sky))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Stat("TIME", fmt(run.elapsedMillis))
                    Stat("MOVES", run.moves.toString())
                    Stat("GRID", "${run.difficulty.gridSize}×${run.difficulty.gridSize}")
                }
            }
            Spacer(Modifier.height(8.dp))
            BlueprintButton("Next puzzle", onNext, Modifier.fillMaxWidth())
            BlueprintButton("Home", onHome, Modifier.fillMaxWidth(), filled = false)
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TesseraType.label.copy(color = TesseraColors.Faint))
        Text(value, style = TesseraType.heading.copy(color = TesseraColors.Paper))
    }
}
```

Note: if the outlined `BlueprintButton("Home", ... filled=false)` renders ink-on-dark and is hard to read, apply the `foreground` param fix noted in Task 16 and pass `TesseraColors.Paper`.

- [ ] **Step 2: Wire into nav**

```kotlin
                composable(Routes.COMPLETE) {
                    com.tessera.puzzle.ui.screens.CompleteScreen(
                        game = game,
                        onNext = {
                            val run = game.lastCompleted.value
                            if (run != null) {
                                nav.navigate(Routes.puzzleSelect(run.difficulty)) {
                                    popUpTo(Routes.HOME)
                                }
                            } else {
                                nav.popBackStack(Routes.HOME, false)
                            }
                        },
                        onHome = { nav.popBackStack(Routes.HOME, false) },
                    )
                }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/CompleteScreen.kt app/src/main/java/com/tessera/puzzle/TesseraApp.kt
git commit -m "Add puzzle complete screen"
```

---

### Task 18: Launcher icon + final verification

**Files:**
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/values/ic_launcher_background.xml` (adaptive icon: steel background + paper 3×3 grid glyph)
- Modify: `AndroidManifest.xml` (`android:icon`, `android:roundIcon`)

**Interfaces:**
- Produces: a valid adaptive launcher icon so the app installs cleanly on API 26+.

- [ ] **Step 1: Add adaptive icon resources**

`app/src/main/res/values/ic_launcher_background.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#5980A6</color>
</resources>
```

`app/src/main/res/drawable/ic_launcher_foreground.xml` — a simple vector 3×3 grid of paper squares centered in the 108dp icon (draw 9 small rects). Minimal valid vector:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <group android:translateX="36" android:translateY="36">
        <path android:fillColor="#F2F2F3" android:pathData="M0,0 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M13,0 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M26,0 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M0,13 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M13,13 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M26,13 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M0,26 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M13,26 h10 v10 h-10 z" />
        <path android:fillColor="#F2F2F3" android:pathData="M26,26 h10 v10 h-10 z" />
    </group>
</vector>
```

`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

- [ ] **Step 2: Reference icon in manifest**

In `<application>`, add `android:icon="@mipmap/ic_launcher"` and `android:roundIcon="@mipmap/ic_launcher"`.

- [ ] **Step 3: Full build + unit tests + lint**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; all unit tests pass (Scramble 4, BoardState 6).

- [ ] **Step 4: Manual play-through verification**

Install on a device/emulator (`./gradlew :app:installDebug` or Android Studio). Confirm the full loop with **airplane mode ON**: Splash → Home → tap Easy → pick a puzzle → board shows sliced photo tiles → tap two tiles to swap → solve the board → Complete shows time/moves → Next returns to the puzzle list. Confirm system back on the board opens Pause, and Exit returns to the puzzle list. Record the result (pass/fail + any issue) in the commit message or a note.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/mipmap-anydpi-v26 app/src/main/res/drawable/ic_launcher_foreground.xml app/src/main/res/values/ic_launcher_background.xml app/src/main/AndroidManifest.xml
git commit -m "Add launcher icon and finalize core slice"
```

---

## Self-Review Notes

**Spec coverage:**
- Splash (spec B01) → Task 12. Home returning-user (B02) → Task 13. Difficulty (B03) → Task 14. Puzzle select (B04) → Task 15. Board/gameplay (D) → Task 16. Pause (D) → Task 16. Complete (D) → Task 17. Swap mechanic, solvability, difficulty sizes → Tasks 3–4. Blueprint visual language (registration frames, meters, duotone, type) → Tasks 8–9. Offline bundled images → Tasks 5–7. Back behaviour (back→Pause; Complete→list) → Tasks 16–17.
- Deferred per spec/Out-of-Scope: camera-create, library management, settings, dark theme, tablet, edge states — intentionally not tasked.

**Type consistency:** `scramble(tileCount, random)`, `BoardState.new/tapTile/withElapsed/isSolved/placedCount/order/selected/moves/elapsedMillis`, `PuzzleCatalog.forDifficulty/byId/all`, `ImageSlicer.slice(context, imageRes, gridSize)`, `GameViewModel.startBoard/tap/restart/exitBoard/board/tiles/lastCompleted/hasBoardInProgress`, `CompletedRun(puzzle, difficulty, elapsedMillis, moves)`, `Routes.puzzleSelect/board`, `Difficulty.gridSize/tileCount/label/level` — all names are consistent across the tasks that produce and consume them.

**Placeholder scan:** all code steps contain concrete implementations; no TODO/TBD. Font bundling and icon-resolution fallbacks are spelled out explicitly rather than left vague.

**Scope:** one cohesive app, one plan; ~18 tasks each ending in a build/test-verifiable deliverable.
