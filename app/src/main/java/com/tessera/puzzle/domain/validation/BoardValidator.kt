package com.tessera.puzzle.domain.validation

/**
 * Validation for persisted board data crossing the data→domain boundary
 * (SECURITY-05 input validation; Resiliency BR-8 corrupt-data handling).
 */
object BoardValidator {

    /**
     * True iff [order] is a permutation of 0 until [tileCount] — each index
     * appears exactly once. Used to reject corrupt SavedBoard rows.
     */
    fun isValidOrder(order: IntArray, tileCount: Int): Boolean {
        if (order.size != tileCount) return false
        val seen = BooleanArray(tileCount)
        for (v in order) {
            if (v < 0 || v >= tileCount || seen[v]) return false
            seen[v] = true
        }
        return true
    }

    /**
     * True iff [selected] (if present) is a valid tile position for [tileCount].
     */
    fun isValidSelection(selected: Int?, tileCount: Int): Boolean =
        selected == null || (selected in 0 until tileCount)
}
