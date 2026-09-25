# claim-api runbook

Write this as if handing the service to a client team on your last day. That framing is the point.

## Service summary
- **What it does:** accepts healthcare claim submissions, stores them, exposes lookup and search.
- **Depends on:** PostgreSQL (claims, members), Redis (member cache).
- **Health:** `GET /actuator/health` — includes `db`, `redis`, and custom `seedData` indicators.
- **Runtime:** Java 17 / Spring Boot 3.3, 2 replicas, ClusterIP Service on 8080.
- **Measured startup: 12.5s.** Any probe delay shorter than this kills a healthy container.


## Alerts (fill in as you build them)
| Alert | Means | First three checks | Fix / escalation |
|---|---|---|---|
| API health DOWN | App is running but can't reach a dependency. Readiness has already pulled pods from the Service | `kubectl get pods` (0/1 = readiness failing, not a crash) · `docker compose ps` · `kubectl logs -l app=claim-api --tail=50` | **Do not restart pods.** Observed: 10-min DB outage → 0/1 with zero restarts, recovered in <10s. Escalate to the dependency owner ||
| p95 latency > 1s | **PENDING** — no baseline yet | — | Blocked on JMeter load test (Track F9). Known: first request to a fresh pod ~140ms vs ~10ms warm, so brief spikes during a rolling update are expected, not incidents |
| 5xx rate > 1% | Unhandled exceptions, or all pods unready | `kubectl logs -l app=claim-api \| grep unhandled_exception` · `kubectl get endpoints claim-api` (empty = nothing serving) · `kubectl get pods` | Errors return RFC 7807 ProblemDetail with no stack trace to caller; server-side detail is in the `unhandled_exception` log line |
| Pod CrashLoopBackOff | Container starts and dies. Unlike health DOWN, this *is* the app's problem | `kubectl describe pod <name>` (Events block first) · `kubectl logs <name> --previous` · check exit code | Liveness firing before 12.5s startup → raise startupProbe. Exit 137 OOMKilled → raise memory limit. ErrImagePull → `kind load` + `imagePullPolicy: IfNotPresent` |
| Redis unreachable | App serves correctly, cache silently does nothing. **No error is produced** | `redis-cli KEYS 'members*'` · `kubectl logs \| grep member_lookup_db` | One log line per *distinct* member is correct; one per *request* means cache miss. Historical cause: @Cacheable self-invocation bypassing the AOP proxy (drill 0c) |

## Rollback procedure
```bash
kubectl rollout history deploy/claim-api     # confirm target revision
kubectl rollout undo deploy/claim-api        # returns in ~0.1s — NOT done yet
kubectl rollout status deploy/claim-api      # ~35s to converge
kubectl get pods                             # confirm 1/1, RESTARTS 0
```
**Measured: ~35s to full convergence**, gated by 12.5s startup plus probe intervals.

**Immediately revert the change in git afterwards.** `rollout undo` does not update
`last-applied-configuration`, so cluster and repo now disagree — the next `kubectl apply`
silently redeploys the version you just rolled back from. It's an emergency lever, not a
deployment mechanism. Rolling back also appends a *new* revision rather than rewinding
history, which is why an undo can itself be undone.

## Known failure modes
| Seen | Cause | Lesson |
|---|---|---|
| Postgres container absent from `compose ps` | int4 overflow in seed data aborted the init script; entrypoint treats that as fatal | Init scripts only re-run on an **empty** volume — needs `down -v` |
| Docker daemon 500 on `_ping` | dockerd not running inside Desktop's VM | Know when to route around a problem instead of repairing it |
| Cache annotation doing nothing | `@Cacheable` called from inside the same bean — AOP proxy bypassed | Same trap silently disables `@Transactional` |
| Host `mvn` failed, container build fine | JAVA_HOME pointed at JDK 25; `--release 17` unsupported | Pinned toolchain in the image is why the container never broke |
| Secrets show base64 | Encoding, not encryption — plaintext also sits in the apply annotation | Needs Sealed Secrets / ESO / SOPS for anything real |
| Readiness probe green, checking nothing | Spring's default readiness group is only `readinessState` | Never trust a probe you haven't watched fail |
| DB outage, zero pod restarts | Liveness correctly excludes dependencies | Had db been in liveness, the whole deployment would crash-loop for the outage's duration |

## Known gaps
- `DB_HOST` is a hardcoded gateway IP (`172.20.0.1`) — lab shortcut, not a pattern
- No HPA, no resource limits (pending Day 12)
- No load-test baseline (blocks the p95 alert row)
- `/actuator/prometheus` exposed but nothing scrapes it