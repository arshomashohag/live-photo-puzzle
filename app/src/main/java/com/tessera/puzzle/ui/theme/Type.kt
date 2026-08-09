package com.tessera.puzzle.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tessera.puzzle.R

@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: Int) = Font(
    R.font.nunito_variable,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Nunito = FontFamily(
    nunito(400),
    nunito(600),
    nunito(700),
    nunito(800),
    nunito(900),
)

/**
 * v2 type scale (Nunito). Names kept from prior design for minimal churn.
 */
object TesseraType {
    val display = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(900), fontSize = 34.sp, letterSpacing = (-0.5).sp)
    val heading = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(800), fontSize = 22.sp, letterSpacing = (-0.3).sp)
    val cardTitle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(800), fontSize = 19.sp)
    val body = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(600), fontSize = 15.sp)
    val label = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(700), fontSize = 11.sp, letterSpacing = 0.5.sp)
    val mono = TextStyle(fontFamily = Nunito, fontWeight = FontWeight(700), fontSize = 13.sp)
}
