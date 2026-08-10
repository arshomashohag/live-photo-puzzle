package com.tessera.puzzle.domain.model

/**
 * Pure hint-count core. Immutable; [use] decrements only while hints remain,
 * so the count never goes negative. No Android types — property-testable.
 */
data class HintState(val remaining: Int) {

    val canUse: Boolean get() = remaining > 0

    fun use(): HintState = if (canUse) HintState(remaining - 1) else this

    companion object {
        const val MAX = 3
        fun fresh(): HintState = HintState(MAX)
    }
}
