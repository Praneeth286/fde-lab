# claim-api runbook

Write this as if handing the service to a client team on your last day. That framing is the point.

## Service summary
- **What it does:** accepts healthcare claim submissions, stores them, exposes lookup and search.
- **Depends on:** PostgreSQL (claims, members), Redis (member cache).
- **Health:** `GET /actuator/health` — includes `db`, `redis`, and custom `seedData` indicators.

## Alerts (fill in as you build them)
| Alert | Means | First three checks | Fix / escalation |
|---|---|---|---|
| API health DOWN | | | |
| p95 latency > 1s | | | |
| 5xx rate > 1% | | | |
| Pod CrashLoopBackOff | | | |
| Redis unreachable | | | |

## Rollback procedure
_(Fill in during Milestone 2 — after you've actually done a `kubectl rollout undo`.)_

## Known failure modes
_(Populate from docs/drills.md as you go. This section is what makes the runbook worth reading.)_
