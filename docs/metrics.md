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
