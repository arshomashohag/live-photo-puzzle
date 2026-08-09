# Requirements Clarification Questions — Tessera Production Build

Please answer each question by filling in the letter choice after the `[Answer]:`
tag. If none of the options match, choose the "Other" option and describe your
preference after the tag. Let me know when you're done.

**Context already locked in earlier discussion (not re-asked):**
- Mechanic: **sliding-swap** (tap two tiles → they slide/animate and exchange;
  all arrangements solvable).
- Custom-photo image storage: **app-internal files (`filesDir`) + Room metadata**.
- DI: **Hilt**. State: **StateFlow**. Camera: **CameraX**. Photo picker:
  **PickVisualMedia**. Prefs: **DataStore**.
- Offline-first, no `INTERNET` permission, only CAMERA permission.

---

## Question 1: Security Extensions
Should security extension rules be enforced for this project?

A) Yes — enforce all SECURITY rules as blocking constraints (recommended for production-grade applications)

B) No — skip all SECURITY rules (suitable for PoCs, prototypes, and experimental projects)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 2: Resiliency Extensions
Should the resiliency baseline be applied to this project? (It is a set of
directional design-time best practices derived from the AWS Well-Architected
Reliability Pillar — oriented toward cloud/distributed workloads.)

A) Yes — apply the resiliency baseline as directional best practices and design-time guidance

B) No — skip the resiliency baseline (this is an offline, on-device Android app with no cloud/distributed backend, so the AWS-reliability-oriented baseline has little to act on)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 3: Property-Based Testing Extension
Should property-based testing (PBT) rules be enforced for this project? (The
puzzle engine — scramble, swap, solvability, completion — is pure algorithmic
logic that suits PBT well.)

A) Yes — enforce all PBT rules as blocking constraints (recommended for projects with business logic, data transformations, serialization, or stateful components)

B) Partial — enforce PBT rules only for pure functions and serialization round-trips

C) No — skip all PBT rules

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 4: Delivery scope of this planning cycle
The production spec is large (7 phases). How should AI-DLC plan/build it?

A) Plan the whole program now (all phases as "units of work"), then build unit-by-unit with approval gates between each

B) Plan + build **one phase at a time** — do full requirements→design→code→test for Phase 1 (architecture + Room persistence) first, then return for the next phase (smaller, safer cycles)

C) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 5: Bundled-puzzle photos for a production release
The 9 bundled photos are currently fetched at build time from picsum.photos
(Unsplash). For a real store release the license/attribution should be clean.
What should the bundled photos be?

A) Keep picsum/Unsplash photos and document their license/attribution in PRIVACY/compliance docs

B) Replace with explicitly CC0 / public-domain photos (e.g. from a known CC0 source) with attribution recorded

C) You will provide your own licensed photos before release; keep current ones as placeholders meanwhile

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 6: Minimum Android version (minSdk)
Currently minSdk = 26 (Android 8.0). The modern photo picker
(PickVisualMedia) is best on API 33+ but has a backport; CameraX supports 21+.
What minSdk should production target?

A) Keep minSdk 26 (Android 8.0) — widest device reach; use PickVisualMedia backport for older devices

B) Raise to minSdk 24 (Android 7.0) — even wider reach

C) Raise to minSdk 29 or higher (Android 10+) — fewer legacy edge cases, smaller test matrix

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 7: App identity for release (applicationId & app name)
Release builds need a stable application id. Current is
`com.tessera.puzzle`, name "Tessera". Keep or change?

A) Keep `com.tessera.puzzle` / "Tessera"

B) Change the applicationId and/or display name (describe in Other)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 8: "Best score" definition
For each puzzle+difficulty, best score is tracked. Rank primarily by:

A) Best (lowest) completion time, with move count as secondary/displayed stat

B) Best (lowest) move count, with time secondary

C) Track and display both independently (best time AND best moves), no single ranking

X) Other (please describe after [Answer]: tag below)

[Answer]:
