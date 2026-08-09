package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * v2 soft shadows. Values approximate the design's colored shadows.
 */
fun Modifier.softShadow(shape: Shape = RoundedCornerShape(22.dp)): Modifier =
    this.shadow(elevation = 8.dp, shape = shape, clip = false)

fun Modifier.cardShadow(shape: Shape = RoundedCornerShape(22.dp)): Modifier =
    this.shadow(elevation = 6.dp, shape = shape, clip = false)

/** Stronger glow for the primary CTA / hero. */
fun Modifier.primaryGlow(shape: Shape = RoundedCornerShape(999.dp)): Modifier =
    this.shadow(elevation = 14.dp, shape = shape, clip = false)
