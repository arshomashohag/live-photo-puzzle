package com.tessera.puzzle

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.ui.screens.BoardScreen
import com.tessera.puzzle.ui.screens.CompleteScreen
import com.tessera.puzzle.ui.screens.DifficultyScreen
import com.tessera.puzzle.ui.screens.HomeScreen
import com.tessera.puzzle.ui.screens.MyPuzzlesScreen
import com.tessera.puzzle.ui.screens.PuzzleSelectScreen
import com.tessera.puzzle.ui.screens.SplashScreen
import com.tessera.puzzle.ui.screens.SettingsScreen
import com.tessera.puzzle.ui.screens.create.CreateFlowHost
import com.tessera.puzzle.domain.model.ThemeResolver
import com.tessera.puzzle.presentation.SettingsViewModel
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraTheme

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val DIFFICULTY = "difficulty"
    const val PUZZLE_SELECT = "puzzleSelect/{difficulty}"
    const val BOARD = "board/{puzzleId}/{difficulty}"
    const val COMPLETE = "complete"
    const val CREATE = "create"
    const val MY_PUZZLES = "myPuzzles"
    const val SETTINGS = "settings"

    fun puzzleSelect(d: Difficulty) = "puzzleSelect/${d.name}"
    fun board(puzzleId: String, d: Difficulty) = "board/$puzzleId/${d.name}"
}

@Composable
fun TesseraApp() {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    val darkTheme = ThemeResolver.isDark(settings.theme, isSystemInDarkTheme())
    TesseraTheme(darkTheme = darkTheme) {
        Surface(color = TesseraColors.Haze, modifier = Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            // One ViewModel shared across destinations (activity-scoped), so the
            // board→complete handoff and Continue state are consistent.
            val vm: GameViewModel = hiltViewModel()
            NavHost(navController = nav, startDestination = Routes.SPLASH) {
                composable(Routes.SPLASH) {
                    SplashScreen(onDone = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    })
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        game = vm,
                        onContinue = { info ->
                            nav.navigate(Routes.board(info.puzzleId, info.difficulty))
                        },
                        onPickDifficulty = { d -> nav.navigate(Routes.puzzleSelect(d)) },
                        onCreate = { nav.navigate(Routes.CREATE) },
                        onMyPuzzles = { nav.navigate(Routes.MY_PUZZLES) },
                        onSettings = { nav.navigate(Routes.SETTINGS) },
                    )
                }
                composable(Routes.DIFFICULTY) {
                    DifficultyScreen(
                        onBack = { nav.popBackStack() },
                        onPick = { d -> nav.navigate(Routes.puzzleSelect(d)) },
                    )
                }
                composable(
                    Routes.PUZZLE_SELECT,
                    arguments = listOf(navArgument("difficulty") { type = NavType.StringType }),
                ) { entry ->
                    val d = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
                    PuzzleSelectScreen(
                        game = vm,
                        difficulty = d,
                        onBack = { nav.popBackStack() },
                        onPick = { puzzleId -> nav.navigate(Routes.board(puzzleId, d)) },
                    )
                }
                composable(
                    Routes.BOARD,
                    arguments = listOf(
                        navArgument("puzzleId") { type = NavType.StringType },
                        navArgument("difficulty") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val pid = entry.arguments!!.getString("puzzleId")!!
                    val d = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
                    BoardScreen(
                        game = vm,
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
                composable(Routes.COMPLETE) {
                    CompleteScreen(
                        game = vm,
                        onNext = { difficulty ->
                            nav.navigate(Routes.puzzleSelect(difficulty)) {
                                popUpTo(Routes.HOME)
                            }
                        },
                        onHome = { nav.popBackStack(Routes.HOME, false) },
                    )
                }
                composable(Routes.CREATE) {
                    CreateFlowHost(
                        gameViewModel = vm,
                        onCancel = { nav.popBackStack() },
                        onReady = { puzzleId, difficulty ->
                            nav.navigate(Routes.board(puzzleId, difficulty)) {
                                popUpTo(Routes.HOME)
                            }
                        },
                    )
                }
                composable(Routes.MY_PUZZLES) {
                    MyPuzzlesScreen(
                        game = vm,
                        onBack = { nav.popBackStack() },
                        // Play a saved custom puzzle at Medium by default; any size
                        // is reachable via the size picker on create/replay.
                        onPlay = { puzzleId ->
                            nav.navigate(Routes.board(puzzleId, Difficulty.MEDIUM))
                        },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
