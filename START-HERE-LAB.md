# Lab session 1 — Days 1–2 (Milestone 0 + start of Milestone 1)

Two sessions, about 4 hours total. At the end you have a running service, a real
performance number, and the first row of `docs/metrics.md` filled in.

Everything in this repo is scaffolding. The *learning* is in sections 3, 4 and 5 below,
which you do yourself. Don't skip them because the app already runs.

---

## Session 1 (~2h) — get it running

### 1. Toolchain check (15 min)

```bash
docker --version && docker compose version
java -version          # must be 17
mvn -version
kubectl version --client
kind version           # install: https://kind.sigs.k8s.io/docs/user/quick-start/
python3 --version      # 3.11+
```

Anything missing, fix it now. Then:

```bash
kind create cluster --name fde
kubectl get nodes      # must say Ready
kind delete cluster --name fde   # yes, delete it. You'll rebuild it in week 2 from memory.
```

**Set your AWS billing alarm today**, before you create a single AWS resource.

### 2. Repo live (15 min)

```bash
cd fde-lab
git init && git add -A && git commit -m "M0: scaffold claim-api, compose stack, seed data"
gh repo create fde-lab --private --source=. --push   # or push to a repo you create in the UI
```

### 3. Bring up the stack (30 min)

```bash
docker compose up -d postgres redis
docker compose ps                     # both should be (healthy)
docker compose logs postgres | tail -20
```

Verify the seed landed:

```bash
docker exec -it fde-postgres psql -U claims -d claims -c "SELECT count(*) FROM claims;"
# expect 300000
```

Run the app:

```bash
cd claim-api && mvn spring-boot:run
curl -s localhost:8080/actuator/health | jq
```

### 4. First acceptance check — the health flip (20 min)

This is the one that teaches something. With the app still running:

```bash
docker compose stop postgres
curl -s localhost:8080/actuator/health | jq     # now DOWN, and the details tell you why
docker compose start postgres
sleep 10 && curl -s localhost:8080/actuator/health | jq
```

**Write down the answer to this before moving on:** if this were a Kubernetes pod, should
that DOWN response restart the container? Why or why not? (This exact question comes back in
week 2, and it's the single most common Kubernetes interview question you'll get.)

### 5. Close out (10 min)

Commit. Write one line in `docs/metrics.md`. Say out loud, in twenty seconds, what you built today.

---

## Session 2 (~2h) — make it slow, then make it fast

### 1. Find the slow query (30 min)

```bash
curl -s -w '\ntotal: %{time_total}s\n' \
  'localhost:8080/claims/search?state=NJ&from=2024-01-01&to=2025-06-30&limit=100' -o /dev/null
```

Run it five times and take the median. **Record it.** Then look at what the database is
actually doing:

```bash
docker exec -it fde-postgres psql -U claims -d claims
```

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.claim_number, c.service_date, c.amount, c.status, m.member_name, m.state
FROM claims c JOIN members m ON m.member_id = c.member_id
WHERE m.state = 'NJ' AND c.service_date BETWEEN '2024-01-01' AND '2025-06-30'
ORDER BY c.service_date DESC LIMIT 100;
```

Read the plan top to bottom and answer these out loud:
- Which node has the largest `actual time`?
- Is there a `Seq Scan`? On which table, and how many rows did it touch vs. return?
- What is the join strategy — hash, merge, or nested loop? Why did the planner choose it?
- Where does the `Sort` happen, and does it spill to disk (`external merge`)?

### 2. Fix it yourself (30 min)

Don't look this up. Decide which index to add from the plan, add it, re-run `EXPLAIN ANALYZE`,
and re-run the curl. If your first index doesn't help, that's the most valuable outcome of the
session — write down *why* it didn't.

```sql
-- your call. Candidates to reason about (don't add them all blindly):
-- CREATE INDEX ... ON claims (member_id);
-- CREATE INDEX ... ON claims (service_date);
-- CREATE INDEX ... ON members (state);
-- a composite? which column order? does ORDER BY change your answer?
ANALYZE;
```

**Record before/after in `docs/metrics.md` rows 2 and 3.** That's your first real number.

### 3. Watch the cache work (20 min)

```bash
# same member twice - watch the app log
curl -s -X POST localhost:8080/claims -H 'Content-Type: application/json' \
  -d '{"claimNumber":"CLM9000000002","memberId":"M00000042","providerNpi":"1234567890","serviceDate":"2025-03-01","amount":10.00}' > /dev/null
curl -s -X POST localhost:8080/claims -H 'Content-Type: application/json' \
  -d '{"claimNumber":"CLM9000000003","memberId":"M00000042","providerNpi":"1234567890","serviceDate":"2025-03-02","amount":11.00}' > /dev/null
docker exec -it fde-redis redis-cli KEYS 'members*'
```

The `member_lookup_db` log line should appear once, not twice. If it appears twice, find out why
(this is a genuinely instructive bug to chase — self-invocation and proxying).

### 4. Containerize and measure (30 min)

```bash
cd claim-api
docker build -t claim-api:0.1.0 .
docker images claim-api                    # record the size
docker history claim-api:0.1.0 | head      # which layer is fattest?
```

Then, for the metric: build a deliberately naive single-stage version (copy the Dockerfile,
strip the second stage, base it on `maven:3.9-eclipse-temurin-17`) and compare sizes.
**Record row 1.** Also confirm you're not running as root:

```bash
docker run --rm claim-api:0.1.0 sh -c 'id'
```

### 5. Close out (10 min)

```bash
docker compose --profile app up -d --build   # whole stack in containers
curl -s localhost:8080/actuator/health | jq
git add -A && git commit -m "M1: index tuning, container hardening, first metrics"
```

---

## Day 2 exit criteria

- [ ] `docker compose up` → healthy API, no manual steps
- [ ] Four numbers in `docs/metrics.md` (rows 1–4)
- [ ] You can explain your `EXPLAIN ANALYZE` plan out loud for two minutes
- [ ] You can answer "should a DOWN health check restart the pod?" without hedging
- [ ] Two commits pushed

## Next

Days 3–5: Kafka producer in `claim-api` + the `claim-worker` consumer with an idempotency key.
Then Milestone 2 (Kubernetes) opens on day 8. Ask me when you're ready and I'll set up the next block.
