# My lab metrics

Real numbers I generated. One row per milestone. Never leave a row blank after its milestone is done.

| # | Metric | Milestone | Before | After | Date |
|---|---|---|---|---|---|
| 1 | Container image size (single-stage vs. multi-stage) | M1 | | | |
| 2 | `/claims/search` p50 latency (no index → index) | M1 | | | |
| 3 | Same query, `EXPLAIN ANALYZE` execution time | M1 | | | |
| 4 | Member lookup, DB hit vs. Redis cache hit | M1 | | | |
| 5 | Cold deploy to a fresh kind cluster | M2 | — | | |
| 6 | Rollback time (`rollout undo` → healthy) | M2 | — | | |
| 7 | Full pipeline duration (commit → running) | M3 | | | |
| 8 | Mean time to recovery across 10 drills | M4 | — | | |
| 9 | Duplicates suppressed by idempotency key | M4 | — | /500 | |
| 10 | p95 under JMeter load (before/after pool tuning) | F9 | | | |

## M1 results (filled 2026-09-09)

| # | Metric | Before | After |
|---|---|---|---|
| 1 | Container image size (single-stage vs multi-stage) | 1.07GB | 373MB |
| 2 | `/claims/search` end-to-end HTTP p50 | — | 10.6ms steady, 176ms cold first request |
| 3 | Same query, EXPLAIN ANALYZE execution time | 34.4ms | 18.3ms |
| 4 | Member lookup, DB hit vs Redis cache | 2 DB hits / 2 requests | 1 DB hit / 2 requests |

**Note on rows 2 vs 3:** the HTTP number is lower than the EXPLAIN ANALYZE number because
EXPLAIN ANALYZE adds per-node instrumentation overhead, and the first psql run had a cold
buffer cache (read=78 in the plan) where later runs hit shared buffers. The 176ms first
request is JVM warm-up — class loading, Hibernate statement prep, first physical pool
connection, JIT still interpreting. That gap is why startupProbe exists.
