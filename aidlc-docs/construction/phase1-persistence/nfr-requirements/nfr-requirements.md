# NFR Requirements — Phase 1 Persistence

Non-functional requirements for the architecture + persistence unit. Each is
verifiable in Build and Test. Cloud/web NFRs are N/A (offline on-device app).

## Performance (NFR-2)
- **P-1**: All DB and file I/O run off the main thread (coroutines + IO
  dispatcher). No disk work on the UI thread.
- **P-2**: In-progress autosave is **debounced (~750 ms, coalescing)** with
  forced saves on onStop/Pause/before-completion (BR-2). No write-per-tap.
- **P-3**: A single SavedBoard upsert is a small, indexed single-row write;
  target well under one frame's budget off-thread. No unbounded caching.
- **P-4**: App start does the seed check once; observers are Flows (no polling).
  No measurable startup regression vs. the slice.

## Security (enforced applicable rules)
- **S-1 (SECURITY-03)**: No user photo bytes, file contents, or PII in logs.
- **S-2 (SECURITY-05)**: Validate inputs at persistence boundaries — SavedBoard
  `order` must be a valid permutation for the tile count; ids/paths validated.
  Room parameterized queries only (no string-concatenated SQL).
- **S-3 (SECURITY-09)**: No stack traces / internal details surfaced to users;
  current supported library versions; no default credentials/secrets.
- **S-4 (SECURITY-10)**: Dependencies pinned via version catalog; **dependency
  vulnerability scan documented in RELEASE_CHECKLIST for Phase 6** (Q3=B); no
  unused dependencies added.
- **S-5 (SECURITY-11)**: Persistence/validation logic isolated in the data layer
  (repositories, mappers, validators) — not scattered into UI.
- **S-6 (SECURITY-15)**: Every DB/file call has explicit error handling; fail
  closed (on load error, discard don't crash); resources (cursors/streams)
  released; a top-level error boundary catches unexpected errors.
- **N/A**: SECURITY-01/02/04/06/07/08/12/13/14 (encryption-in-transit, network
  intermediaries, HTTP headers, IAM, network config, endpoint authz, user-auth,
  CDN/SRI, alerting) — no network/server/multi-user surface.

## Resiliency (enforced on-device subset)
- **R-1 (recoverability)**: In-progress board and stats survive **process
  death / app restart** (durable Room + debounced/forced saves).
- **R-2 (graceful degradation)**: Corrupt/malformed SavedBoard or missing image
  file → discard the record + friendly non-blocking notice (BR-8); never crash.
- **R-3 (observability)**: Failures are handled crash-safely without logging
  PII/photos; diagnostics use non-sensitive identifiers only.
- **N/A**: Cloud/DR/HA/multi-region practice areas.

## Maintainability & Testability (NFR-1, NFR-8)
- **M-1**: Layer separation — pure `domain/` (no Android imports), `data/`
  (Room/DataStore/files), `presentation/` (StateFlow VMs), `ui/`.
- **M-2**: Repository pattern; interfaces in domain, impls in data, bound by
  Hilt.
- **M-3 (PBT)**: Kotest property tests for engine invariants + persistence
  round-trips (per functional-design PBT-01 table); seeded/reproducible
  (PBT-08); complements example-based tests (PBT-10).
- **M-4**: Room schema migration policy = **destructive during development**
  (Q1=A); explicit migrations authored before first production release
  (Phase 6). Schema is exported (`room.schemaLocation`) so migrations are
  reviewable.

## Reliability (NFR-2)
- **Rel-1**: `suspend`/`Flow` repository API; structured concurrency via
  `viewModelScope`/injected dispatchers.
- **Rel-2**: No behavior regressions — existing engine tests and app flows keep
  passing.

## Usability
- **U-1**: Persistence is invisible when it works; the only user-visible change
  this phase is durable Continue/stats and the corrupt-data notice (BR-8).

## Verification Gates (checked in Build and Test)
- Unit + PBT (engine + mapping) pass; Room repository tests pass; app builds.
- Restart-survival verified (in-progress board + best time persist across
  process death).
- Security compliance summary: S-1..S-6 compliant; listed rules N/A.
- No main-thread I/O (inspection + StrictMode-friendly design).
