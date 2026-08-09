package com.tessera.puzzle.domain.model

/**
 * Adaptive window-size buckets (mapped from Material3 WindowSizeClass in the UI
 * layer).
 */
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }

/**
 * Layout parameters for a window size. Pure so it is unit/property-testable.
 *
 * @property maxContentWidthDp content max width (Int.MAX_VALUE = unbounded)
 * @property gridColumns columns for puzzle/library grids
 * @property boardMaxDp maximum board edge
 */
data class LayoutSpec(
    val maxContentWidthDp: Int,
    val gridColumns: Int,
    val boardMaxDp: Int,
)

fun layoutSpec(size: WindowSize): LayoutSpec = when (size) {
    WindowSize.COMPACT -> LayoutSpec(maxContentWidthDp = Int.MAX_VALUE, gridColumns = 2, boardMaxDp = 560)
    WindowSize.MEDIUM -> LayoutSpec(maxContentWidthDp = 840, gridColumns = 2, boardMaxDp = 560)
    WindowSize.EXPANDED -> LayoutSpec(maxContentWidthDp = 840, gridColumns = 3, boardMaxDp = 560)
}
