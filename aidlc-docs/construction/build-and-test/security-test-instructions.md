# Security Test Instructions

Verifies the applicable Security Baseline rules for this offline on-device unit.

## SECURITY-05 — Input validation
- **Test**: `MapperPropertiesTest` ("malformed order CSV maps to null", "unknown
  difficulty maps to null") + `PuzzlePersistenceTest` ("corruptOrder discarded").
- **Manual**: confirm `BoardValidator.isValidOrder` rejects non-permutations.
- Room uses parameterized queries only (no string-concatenated SQL) — inspect DAOs.

## SECURITY-15 — Fail-safe & resource cleanup
- **Review**: repositories wrap DB/file calls; `BoardRepositoryImpl.loadBoard`
  discards invalid rows (fail-closed) rather than throwing; `PuzzleFileStore`
  swallows delete failures after logging (no content).
- **Manual**: delete a custom puzzle whose file is already missing → no crash,
  DB row still removed.

## SECURITY-03 — No sensitive logging
- **Review**: `grep -rn "Log\." app/src/main` — confirm no photo bytes, file
  contents, or PII are logged. Only non-sensitive messages/ids.
- Release builds strip debug logs (enforced in Phase 5).

## SECURITY-10 — Dependency hygiene
- Dependencies pinned via `gradle/libs.versions.toml`.
- Vulnerability scan is **documented for Phase 6** (RELEASE_CHECKLIST) per the
  NFR decision (Q3=B).
- **Check for unused deps**: `./gradlew :app:dependencies` review.

## SECURITY-09 — No internal details to users
- **Review**: user-facing error copy is generic ("Couldn't restore your last
  puzzle"); no stack traces surfaced.

## Static analysis
```bash
./gradlew :app:lintDebug
```
- **Expected**: BUILD SUCCESSFUL, 0 lint errors.

## N/A (offline on-device app)
SECURITY-01/02/04/06/07/08/12/13/14 (encryption-in-transit, network
intermediaries, HTTP headers, IAM, network config, endpoint authz, user-auth,
CDN/SRI, alerting) — no network/server/multi-user surface.
