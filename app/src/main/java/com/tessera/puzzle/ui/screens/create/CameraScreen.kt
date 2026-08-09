package com.tessera.puzzle.ui.screens.create

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tessera.puzzle.camera.CameraController
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    controller: CameraController,
    onCaptured: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }
    val previewView = remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(previewView.value) {
        previewView.value?.let { controller.bind(lifecycleOwner, it) }
    }

    Box(Modifier.fillMaxSize().background(TesseraColors.SplashBg)) {
        AndroidView(
            factory = { ctx -> PreviewView(ctx).also { previewView.value = it } },
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "CANCEL",
                style = TesseraType.label.copy(color = TesseraColors.Paper),
                modifier = Modifier.clickable { onCancel() }.padding(8.dp),
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(72.dp)
                        .border(3.dp, TesseraColors.Paper, CircleShape)
                        .background(if (capturing) TesseraColors.Steel else TesseraColors.Paper.copy(alpha = 0.2f), CircleShape)
                        .clickable(enabled = !capturing) {
                            capturing = true
                            scope.launch {
                                runCatching { controller.capture() }
                                    .onSuccess { onCaptured(it) }
                                    .also { capturing = false }
                            }
                        }
                        .semantics { contentDescription = "Take photo" },
                )
            }
        }
    }
}
