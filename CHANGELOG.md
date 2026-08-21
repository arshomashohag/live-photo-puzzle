# Changelog

All notable changes to Tessera are documented here. This project adheres
to [Keep a Changelog](https://keepachangelog.com/) and
[Semantic Versioning](https://semver.org/).

## [1.0.3] - 2026-08-21

### Added
- Name your puzzle: after capturing or picking a photo, a new step lets you
  name the puzzle. The name is previewed over the photo on a single line, and
  leaving it blank auto-names the puzzle.
- Gallery shortcut on the camera screen, so you can switch to an existing
  photo without leaving the capture view.

### Changed
- Custom puzzles now show their photo thumbnail in the puzzle picker instead
  of a plain coloured tile.
- The puzzle board is centered on screen, with a small gap and rounded corners
  between tiles so each piece reads as distinct.
- Touching a tile now gently bounces the tiles it can swap with, cueing which
  moves are available.
- Creating a custom puzzle opens the camera directly (or the photo picker on
  camera-less devices), removing the intermediate chooser screen. The camera's
  Cancel is now a back button that returns Home.

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
