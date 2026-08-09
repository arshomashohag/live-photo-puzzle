package com.tessera.puzzle.domain.model

import com.tessera.puzzle.domain.model.persistence.ThemeMode

/**
 * Pure resolution of the effective dark-mode boolean from the user's chosen
 * ThemeMode and whether the OS is currently in dark mode.
 */
object ThemeResolver {
    fun isDark(mode: ThemeMode, systemInDark: Boolean): Boolean = when (mode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
