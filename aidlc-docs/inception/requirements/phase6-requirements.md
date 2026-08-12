# Phase 6 — Docs & Compliance: Requirements

## Intent
Produce the documentation required to (a) publish Tessera to Google Play and
(b) let a new contributor build and understand the app. No production code
changes — documentation only. This closes the gap between "release-capable"
(Phase 5) and "submittable to Play."

## Verified facts (grounded in the codebase, not assumed)
- **No `INTERNET` permission** in the manifest; **no** network/analytics/ad/
  crash-reporting libraries (no Retrofit/OkHttp/Ktor/Firebase/Crashlytics).
  → The app is fully **offline**; **no data leaves the device**.
- **Permissions declared**: `CAMERA` (capture a photo for a custom puzzle) and
  `VIBRATE` (haptic feedback, user-toggleable).
- **Local storage only**: DataStore (settings), Room + app files (custom
  puzzle images/metadata). All under app-private storage.
- **Play data-safety consequence**: **no data collected, no data shared** — the
  strongest/simplest data-safety posture.
- Release build config, signing, dep-scan, versionName 1.0.0 already exist
  (Phase 5). No README currently exists at repo root.

## Functional Requirements
- **FR-1 README.md** — project overview, feature list, tech stack, build/run
  instructions (JDK 21, `ANDROID_HOME`, gradle commands), test/lint commands,
  link to `docs/RELEASE_SIGNING.md`.
- **FR-2 PRIVACY.md** — plain-language privacy policy: no data collected, no
  network, camera photos stored locally only, how to delete data (uninstall /
  in-app delete). Suitable to host as the Play "privacy policy URL".
- **FR-3 PLAY_STORE_COMPLIANCE.md** — Play Data Safety form answers
  (collected: none; shared: none), permissions justification (CAMERA, VIBRATE),
  target audience/content rating notes, target API level (35) confirmation.
- **FR-4 RELEASE_CHECKLIST.md** — ordered pre-upload gate: bump versionCode,
  run tests+lint, run `dependencyCheckAnalyze` + review, build signed AAB,
  verify signed, upload, privacy URL set, data-safety form filled.
- **FR-5 ARCHITECTURE.md** — module/layer overview (UI/Compose → ViewModel →
  domain pure cores → data), key patterns (UDF/StateFlow, pure decision cores +
  PBT, Hilt DI), where custom-puzzle storage lives.

## Non-Functional Requirements
- **NFR-1 Accuracy** — every compliance claim must match the code (offline,
  permissions, storage). No aspirational/false statements (a wrong data-safety
  answer is a Play policy violation).
- **NFR-2 Self-contained** — docs reference real files/commands that exist and
  work; build commands must be the ones actually used this repo.

## Scope
- **In**: the five markdown docs above, at repo root (except RELEASE_SIGNING.md
  which already lives in `docs/`). AI-DLC state/audit updates.
- **Out**: code changes, screenshots/store-listing graphics, actual Play
  Console submission, hosting the privacy policy (user action), R8/minify.

## Extension compliance (this stage)
- **Security Baseline**: docs must not leak secrets; must state the real
  (no-collection) data posture — supports SECURITY-12/-10 transparency. Others
  N/A (no code surface changes).
- **Resiliency / Property-Based Testing**: N/A (documentation-only stage; no
  logic to test or make resilient).

## Open questions for the user (approval gate)
1. **Doc set** — all five (README, PRIVACY, PLAY_STORE_COMPLIANCE,
   RELEASE_CHECKLIST, ARCHITECTURE), or a subset?
2. **Privacy policy hosting** — I'll write `PRIVACY.md`; hosting it at a public
   URL for the Play listing is your action (GitHub Pages / gist / site). OK?
3. **Author/contact placeholder** — PRIVACY/Play docs usually need a contact
   email. Use `shohagsiraj.ru@gmail.com`, or a placeholder `CONTACT_EMAIL`?
