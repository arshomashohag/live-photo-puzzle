# NFR Design Patterns — Phase 1 Persistence

Concrete patterns that satisfy the NFRs. On-device, single-user, offline —
distributed patterns (queues, circuit breakers, caches, autoscaling) are not
used because there is no corresponding surface.

## Resilience Patterns

### RP-1 Write-through durable persistence (recoverability, R-1)
- The Room DB is the single source of truth for saved boards, best scores, and
  puzzle records. UI state (StateFlow) is derived from DB Flows, so a
  process kill and relaunch rebuilds identical state from disk.
- Debounced + forced saves (BR-2) ensure the durable copy is current at all
  lifecycle-critical moments.

### RP-2 Validate-on-read + quarantine-discard (graceful degradation, R-2/BR-8)
- Every record crossing the data→domain boundary passes a validator
  (`isValidOrder`, file-existence check). Invalid records are **discarded**
  (bad SavedBoard row deleted) rather than propagated — the app degrades to
  "no Continue" instead of crashing.
- A one-shot, non-blocking UI notice signals the discard (Home banner), cleared
  after shown.

### RP-3 Fail-closed error boundary (SECURITY-15)
- Repository methods wrap DB/file calls; on failure they return a safe default
  (null / empty / no-op) and never leak exceptions to the UI. A top-level
  coroutine exception handler + Compose-level guard catches the unexpected and
  shows a generic message.

## Performance Patterns

### PP-1 Dispatcher confinement (P-1)
- All DB/file work runs on an injected IO dispatcher (`@IoDispatcher`), never the
  main thread. Repositories expose `suspend`/`Flow`; ViewModels collect in
  `viewModelScope`.

### PP-2 Debounced write coalescing (P-2/P-3, BR-2)
- The ViewModel holds a `MutableStateFlow<BoardState>`; a save pipeline uses
  `debounce(750ms)` + `mapLatest { repo.saveBoard(...) }` so bursts of taps
  collapse to one write. `conflate()` guards against backpressure. Forced saves
  bypass the debounce on onStop/Pause/complete.

### PP-3 Cold-Flow, no-polling reads (P-4)
- Home/Continue/stats are Room `Flow` queries turned into `StateFlow` via
  `stateIn(SharingStarted.WhileSubscribed)`. No timers/polling; updates are
  push-based and stop when unsubscribed.

## Security Patterns

### SP-1 Single validation boundary (SECURITY-05/11)
- Input validation for persisted data is centralized in the data layer
  (mapper/validator), not duplicated in UI. Room parameterized queries only.

### SP-2 Safe logging (SECURITY-03)
- A thin logging helper forbids logging photo bytes/paths' contents/PII; only
  non-sensitive ids/counters allowed. Debug logs compiled out in release
  (enforced Phase 5).

### SP-3 Generic user-facing errors (SECURITY-09)
- User-visible error text is generic ("Couldn't restore your last puzzle" /
  "Something went wrong"). No stack traces, class names, paths, or SQL surfaced.

## Reliability Pattern

### RelP-1 Structured concurrency
- All async work is scoped (`viewModelScope`, structured `withContext`); no
  free-running global coroutines. Cancellation on scope end prevents leaks
  (e.g. the timer/save pipeline cancels with the ViewModel).

## Maintainability Pattern

### MP-1 Ports & adapters (repository pattern)
- Domain defines repository **interfaces** (ports); data provides Room/DataStore
  **implementations** (adapters); Hilt binds them. Enables JVM tests against
  fakes and Room in-memory tests against the real adapter.

## Not Used (justified)
- **Caching layer**: dataset is tiny and already in a fast local DB — an extra
  cache would add complexity and staleness risk. N/A.
- **Circuit breaker / retry / bulkhead**: no remote calls. N/A.
- **Queues / backpressure infra**: `conflate()`/`debounce` on a single local
  writer suffices. N/A.
