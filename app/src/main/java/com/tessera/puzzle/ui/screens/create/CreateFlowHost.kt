package com.tessera.puzzle.ui.screens.create

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.presentation.CreateState
import com.tessera.puzzle.presentation.CreateViewModel

/**
 * Hosts the custom-puzzle create flow, switching UI on CreateState. Owns the
 * CAMERA permission launcher and the system photo picker (PickVisualMedia).
 * On Ready, starts the board via the shared GameViewModel and hands back.
 */
@Composable
fun CreateFlowHost(
    gameViewModel: GameViewModel,
    onCancel: () -> Unit,
    onReady: (String, Difficulty) -> Unit,
) {
    val vm: CreateViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) vm.cameraReady() else vm.permissionDenied()
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) vm.onPicked(uri) else vm.openChooser()
    }

    // React to state transitions that need Android launchers.
    LaunchedEffect(state) {
        when (val s = state) {
            is CreateState.RequestingPermission ->
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            is CreateState.Ready -> {
                gameViewModel.startBoard(s.puzzleId, s.difficulty)
                onReady(s.puzzleId, s.difficulty)
                vm.consumeReady()
            }
            else -> Unit
        }
    }

    when (val s = state) {
        CreateState.Chooser -> CreateChooserScreen(
            onTakePhoto = { vm.chooseCamera() },
            onChoosePhoto = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onBack = onCancel,
        )
        CreateState.RequestingPermission -> CreateChooserScreen({}, {}, onCancel)
        CreateState.PermissionDenied -> PermissionNeededScreen(
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:${context.packageName}".toUri(),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            onChoosePhoto = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onBack = onCancel,
        )
        CreateState.Camera -> CameraScreen(
            controller = vm.cameraController,
            onCaptured = { vm.onCaptured(it) },
            onCancel = { vm.openChooser() },
        )
        is CreateState.Review -> ReviewScreen(
            source = s.source,
            onRetake = { vm.retake() },
            onAccept = { vm.accept() },
        )
        is CreateState.PickSize -> PickSizeScreen(
            onPick = { vm.pickSize(it) },
            onBack = { vm.retake() },
        )
        is CreateState.Generating -> ImportGeneratingScreen()
        is CreateState.Error -> CreateErrorScreen(s.message, onDismiss = { vm.dismissError() })
        is CreateState.Ready -> ImportGeneratingScreen()
    }
}
