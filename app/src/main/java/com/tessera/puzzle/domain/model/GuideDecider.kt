package com.tessera.puzzle.domain.model

/**
 * Pure first-run guide decision core. The swipe-guide overlay is shown only
 * the first time a puzzle is played after install: when it has never been
 * shown AND a board is active. No Android types — property-testable.
 */
object GuideDecider {

    fun shouldShow(guideShown: Boolean, hasActiveBoard: Boolean): Boolean =
        !guideShown && hasActiveBoard
}
