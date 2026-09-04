# fde-lab — claim-api

A healthcare claims service built as the spine project for a 30-day Forward Deployment Engineer prep plan.
Java 17 · Spring Boot 3.3 · PostgreSQL · Redis · Docker · (Kubernetes and CI/CD land in weeks 2–3)

## Run it in three commands

```bash
docker compose up -d postgres redis     # seeds 20k members + 300k claims on first boot
cd claim-api && ./mvnw spring-boot:run  # or: mvn spring-boot:run
curl -s localhost:8080/actuator/health | jq
```

First boot takes ~30s while the seed script runs. `docker compose logs -f postgres` shows progress.

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
