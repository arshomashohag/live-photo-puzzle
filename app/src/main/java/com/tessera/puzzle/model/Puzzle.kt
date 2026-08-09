package com.tessera.puzzle.model

import androidx.annotation.DrawableRes

data class Puzzle(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
)
