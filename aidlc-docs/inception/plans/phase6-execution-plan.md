# Phase 6 — Docs & Compliance: Execution Plan

## Decisions (user-approved)
- **Doc set**: all five (README, PRIVACY, PLAY_STORE_COMPLIANCE,
  RELEASE_CHECKLIST, ARCHITECTURE).
- **Contact**: `shohagsiraj.ru@gmail.com` (used in PRIVACY + compliance docs).
- **Hosting**: user hosts the privacy policy URL (out of scope for this phase).

## AI-DLC stages
- **EXECUTE**: Requirements Analysis (done), Workflow Planning (this plan),
  Code Generation (docs), Build and Test (verify commands/links).
- **SKIP** (rationale): Reverse Engineering (already known), User Stories
  (no user-facing feature), Application Design / Units Generation /
  Functional Design / NFR Requirements / NFR Design / Infrastructure Design
  (documentation-only; no code, no logic, no infra).

## Deliverables (all repo root unless noted)
| # | File | Purpose |
|---|------|---------|
| 1 | `README.md` | overview, features, tech stack, build/run/test commands, doc links |
| 2 | `PRIVACY.md` | privacy policy: no collection, offline, local-only, deletion; contact email |
| 3 | `PLAY_STORE_COMPLIANCE.md` | Data Safety answers, permissions justification, target API/content rating |
| 4 | `RELEASE_CHECKLIST.md` | ordered pre-upload gate (versionCode, tests, dep-scan, signed AAB, upload) |
| 5 | `ARCHITECTURE.md` | layers, patterns (UDF/StateFlow, pure cores + PBT, Hilt), storage |

## Accuracy anchors (verified facts to reflect exactly)
- Offline: no `INTERNET` permission; no network/analytics/ad/crash libs.
- Permissions: `CAMERA` (photo capture for custom puzzles), `VIBRATE` (haptics).
- Storage: DataStore (settings) + Room/app-files (custom puzzles), app-private.
- Data safety: **no data collected, no data shared, none leaves device.**
- Build: JDK 21, `ANDROID_HOME`, AGP 8.6.1, minSdk 29 / target 35, versionName 1.0.0.
- Existing: `docs/RELEASE_SIGNING.md`, OWASP `dependencyCheckAnalyze`, 53 unit tests.

## Verification (Build and Test stage)
1. Every gradle command quoted in the docs runs (`testDebugUnitTest`,
   `lintDebug`, `bundleRelease`, `dependencyCheckAnalyze`) — already proven.
2. Every internal doc link resolves to a file that exists.
3. No secrets / no false compliance claims (cross-check against the manifest).

## Out of scope
Code changes, store graphics/screenshots, hosting the privacy URL, actual Play
submission, R8/minify.
