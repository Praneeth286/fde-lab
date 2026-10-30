# fde-lab — claim-api

A healthcare claims service built as the spine project for a 30-day Forward Deployment Engineer prep plan.
Java 17 · Spring Boot 3.3 · PostgreSQL · Redis · Docker · Kubernetes (kind) · (CI/CD and Kafka still to come)

## Run it in three commands

```bash
docker compose up -d postgres redis     # seeds 20k members + 300k claims on first boot
cd claim-api && ./mvnw spring-boot:run  # or: mvn spring-boot:run
curl -s localhost:8080/actuator/health | jq
```

First boot takes ~30s while the seed script runs. `docker compose logs -f postgres` shows progress.

## Run it on Kubernetes

Manifests are in `k8s/`: a ConfigMap and Secret for config, a 2-replica Deployment, a ClusterIP Service on 8080, and an HPA. Postgres and Redis still run in Docker Compose.

```bash
docker compose up -d postgres redis
docker build -t claim-api:0.1.1 claim-api
kind load docker-image claim-api:0.1.1
kubectl apply -f k8s/
kubectl rollout status deploy/claim-api
```

What's in the Deployment:

- **Probes:** `startupProbe` and `livenessProbe` on `/actuator/health/liveness`, `readinessProbe` on `/actuator/health/readiness`. Liveness deliberately excludes the database, so a DB outage pulls pods out of the Service without restarting them.
- **Resources:** requests of 100m CPU / 512Mi memory, a 768Mi memory limit, and no CPU limit (on purpose).
- **Autoscaling:** HPA from 2 to 6 replicas at 50% average CPU.

`DB_HOST` in the ConfigMap points at the kind network gateway, which is a lab shortcut. See `docs/runbook.md` for known gaps.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| `POST` | `/claims` | Validated submission; 409 on duplicate claim number |
| `GET` | `/claims/{id}` | 404 via `ProblemDetail` when missing |
| `GET` | `/claims/search?state=NJ&from=2024-01-01&to=2024-06-30` | The deliberately slow one — see Milestone 1 |
| `GET` | `/actuator/health` | Includes `db`, `redis`, and custom `seedData` indicators |
| `GET` | `/actuator/prometheus` | Metrics, used from week 3 onward |

## Try it

```bash
curl -s -X POST localhost:8080/claims -H 'Content-Type: application/json' -d '{
  "claimNumber":"CLM9000000001","memberId":"M00000042","providerNpi":"1234567890",
  "serviceDate":"2025-03-01","amount":420.75}' | jq

# validation failure — look at the shape of the error
curl -s -X POST localhost:8080/claims -H 'Content-Type: application/json' -d '{
  "claimNumber":"nope","memberId":"","providerNpi":"12","serviceDate":"2999-01-01","amount":-5}' | jq
```

## Lab notes

The point of this repo is the operational record, not just the code:

| File | What's in it |
|---|---|
| [`docs/metrics.md`](docs/metrics.md) | Real before/after numbers: image size (1.07GB → 373MB), query latency, cache hit rates, rollback time, HPA behaviour |
| [`docs/drills.md`](docs/drills.md) | A log of deliberate (and some accidental) failures, each with symptom, diagnosis and fix |
| [`docs/runbook.md`](docs/runbook.md) | Hand-off runbook: alerts, rollback procedure, known failure modes and gaps |
| [`START-HERE-LAB.md`](START-HERE-LAB.md) | Step-by-step guide for the first two lab sessions |

## Architecture (end state, day 26)

```
POST /claims -> claim-api (Java 17) -> Postgres
                     |             \-> Redis (member cache)
                     | produces claims.submitted
                     v
                   Kafka -> claim-worker (idempotent consumer) -> Postgres
                                     |
                             claim-insights (FastAPI, JWT) -> /stats
```

All of it containerized, deployed to a kind cluster by GitHub Actions, and broken on purpose once a week.

## What I'd do differently at production scale
_(Write this section yourself at the end of week 4. Interviewers read it first.)_
