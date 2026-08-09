package com.tessera.puzzle.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.tessera.puzzle.ui.theme.rememberReducedMotion
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    val visible = remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible.value || reducedMotion) 1f else 0f,
        animationSpec = tween(if (reducedMotion) 0 else 400),
        label = "splash",
    )

    LaunchedEffect(Unit) {
        visible.value = true
        delay(if (reducedMotion) 300 else 750)
        onDone()
    }

    Box(
        Modifier.fillMaxSize().background(TesseraColors.SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                Modifier.alpha(alpha).size(96.dp).background(TesseraColors.Paper),
                contentAlignment = Alignment.Center,
            ) {
                GridGlyph()
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TESSERA",
                    style = TesseraType.display.copy(color = TesseraColors.Paper, fontSize = 46.sp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "PHOTO PUZZLE",
                    style = TesseraType.label.copy(color = TesseraColors.Sky),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun GridGlyph() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val opacities = listOf(
            listOf(1f, 0.5f, 1f),
            listOf(0.5f, 1f, 0.28f),
            listOf(1f, 0.28f, 0.5f),
        )
        opacities.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { o ->
                    Box(
                        Modifier
                            .size(16.dp)
                            .alpha(o)
                            .background(TesseraColors.SplashBg),
                    )
                }
            }
        }
    }
}
