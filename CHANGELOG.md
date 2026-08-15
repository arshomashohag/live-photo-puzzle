# Changelog

All notable changes to Tessera are documented here. This project adheres
to [Keep a Changelog](https://keepachangelog.com/) and
[Semantic Versioning](https://semver.org/).

## [1.0.2] - 2026-08-15

### Added
- First-run swipe guide: the first time you play a puzzle, a light coach-mark
  over the board shows an animated swipe cue and how to swap tiles. It appears
  once and never returns.

### Changed
- Moved the Hint and Pause controls to the bottom of the board screen.

## [1.0.1] - 2026-08-15

### Changed
- Target Android 16 (API level 36) to meet Google Play's target-API
  requirement; upgraded the Android Gradle Plugin to 8.9.1. No user-facing
  feature or behaviour changes.

## [1.0.0] - 2026-08-14

### Added
- Slide-tile photo puzzles with Easy / Medium / Hard difficulty levels.
- Built-in puzzle set plus custom puzzles from the camera or photo picker.
- Directional swipe controls with slide animation; adjacent-only swaps.
- Hint overlay and a full-image reveal on solve.
- Sound and haptic feedback, dark theme, and adaptive layouts.
- Fully offline: no data collected, no data shared, no network permission.
