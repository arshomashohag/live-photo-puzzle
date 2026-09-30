# Changelog

All notable changes to Tessera are documented here. This project adheres
to [Keep a Changelog](https://keepachangelog.com/) and
[Semantic Versioning](https://semver.org/).

## [1.0.6] - 2026-09-30

### Changed
- Much smaller download and install: the app is now a fraction of its previous
  size, so it downloads faster and takes up far less space on your device.
- Puzzle thumbnails in the picker and My Puzzles now load using less memory,
  which keeps scrolling smooth on devices with less RAM.

## [1.0.5] - 2026-09-30

### Fixed
- 16 KB memory page size support: the app now runs on devices configured with
  16 KB memory pages, which Android 15 and later support. This was resolved by
  updating CameraX to 1.4.2, whose bundled native library is aligned for 16 KB
  pages; the 1.3.4 release it replaces was aligned only for 4 KB.

## [1.0.4] - 2026-08-22

### Added
- Choose your sounds: Settings now lets you pick from five move sounds
  (Soft tick, Pop, Click, Marimba, Glass) and four completion sounds
  (Arpeggio, Sparkle, Chime, Fanfare). Tap an option to hear it and select it.

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
