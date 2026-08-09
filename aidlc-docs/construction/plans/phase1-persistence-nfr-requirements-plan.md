# NFR Requirements Plan — Phase 1: Architecture Hardening + Room Persistence

**Unit**: `phase1-persistence`

## Plan Checklist
- [ ] Capture tech-stack decisions (Hilt, Room, KSP, DataStore, Kotest) with versions
- [ ] Performance NFRs (off-main-thread I/O, autosave write budget, startup)
- [ ] Security NFRs (applicable SECURITY rules for this unit)
- [ ] Resiliency NFRs (process-death survival, corrupt-data handling)
- [ ] Maintainability/Testability NFRs (layering, PBT, coverage targets)
- [ ] Reliability NFRs (fail-safe error handling, resource cleanup)
- [ ] Collect answers; resolve ambiguities
- [ ] Generate nfr-requirements.md + tech-stack-decisions.md
- [ ] Compliance summary (Security/Resiliency/PBT)

---

## Clarifying Questions

Answer each after the `[Answer]:` tag. Most NFRs are already fixed by
requirements.md; these are the few open knobs.

## Question 1: Room schema migration policy for pre-release iteration
During active development, when the DB schema changes, how should Room handle it?

A) `fallbackToDestructiveMigration` during development (wipe+rebuild local DB on schema change) — simplest while iterating pre-release; write real migrations before the first production release (Phase 6)

B) Write explicit Room migrations from the very first schema version now — more upfront work, but migration-ready immediately

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 2: Autosave-on-every-move performance safeguard
Q1 of functional design chose autosave on every move. To keep writes cheap and
off the UI thread, which safeguard?

A) Each save is a suspend upsert on the IO dispatcher (Room handles it); trust Room's speed for a single-row write — simplest, adequate for one small row per move

B) In addition, coalesce rapid consecutive moves so at most one write is in flight at a time (conflate) — extra safety against fast tapping

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 3: Dependency vulnerability scanning (SECURITY-10)
SECURITY-10 requires a dependency vulnerability scan be configured or
documented. For this local Gradle project, which approach?

A) Document the scan command in build instructions and add the OWASP Dependency-Check Gradle plugin config now (runnable locally/CI)

B) Document a manual periodic check (e.g. `gradlew dependencies` review + advisory monitoring) in RELEASE_CHECKLIST for Phase 6; don't add a plugin yet

X) Other (please describe after [Answer]: tag below)

[Answer]:
