package com.tessera.puzzle.domain.model

import androidx.annotation.DrawableRes

/**
 * A playable puzzle image. Exactly one source is set: a bundled drawable
 * ([imageRes] != 0) or an app-internal file ([imagePath] != null) for custom
 * photos. The engine stays pure — [imagePath] is just a path string.
 */
data class Puzzle(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int = 0,
    val imagePath: String? = null,
)
