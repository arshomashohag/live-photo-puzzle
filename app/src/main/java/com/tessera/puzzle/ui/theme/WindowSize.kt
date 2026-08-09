package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.tessera.puzzle.domain.model.WindowSize
import com.tessera.puzzle.domain.model.layoutSpec

/**
 * Current window size bucket from the Material3 adaptive info.
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val width = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    return when (width) {
        WindowWidthSizeClass.EXPANDED -> WindowSize.EXPANDED
        WindowWidthSizeClass.MEDIUM -> WindowSize.MEDIUM
        else -> WindowSize.COMPACT
    }
}

/**
 * Centers content and caps its width per the current window size (BR3-3).
 */
@Composable
fun ContentContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spec = layoutSpec(rememberWindowSize())
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val inner = if (spec.maxContentWidthDp == Int.MAX_VALUE) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.widthIn(max = spec.maxContentWidthDp.dp).fillMaxWidth()
        }
        Box(inner) { content() }
    }
}
