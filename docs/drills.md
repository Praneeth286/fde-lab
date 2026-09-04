# Drill log

One entry per deliberate failure. Fill it in *while recovering*, not after — the messy version is the honest one.

## Template

### Drill N — <what I broke>
- **Date / time to recovery:**
- **Symptom (what a user or dashboard would see):**
- **First three commands I ran:**
- **What I believed the cause was, and what it actually was:**
- **Fix:**
- **Prevention (what would have caught this earlier):**
- **The sentence I'd say in an interview:**

---

<!-- Drill 1 will be a wrong DB password in the Kubernetes Secret. That comes in Milestone 2. -->
### Drill 0 — Postgres container died during seed (unplanned, real)
- **Date / time to recovery:** 2026-09-04 · ~25 min
- **Symptom:** `docker compose ps` showed only Redis. Postgres was absent
  entirely — not unhealthy, not restarting. The app couldn't start.
- **First three commands I ran:**
  1. `docker compose ps -a` (to see exited containers, not just running)
  2. `docker compose logs postgres`
  3. `grep 7919 db/init/02-seed.sql` (after the log named the statement)
- **What I believed vs. what it was:** I assumed a port conflict on 5432 or a
  resource limit. Actual cause: `generate_series` returns int4, and at
  g=300000 the expression `g * 7919` = 2,375,700,000 overflowed the 2,147,483,647
  ceiling. The init script aborted mid-insert, and the entrypoint treats a failed
  init script as fatal, so the container exited.
- **Fix:** Cast to bigint (`g::bigint * 7919`), then `docker compose down -v`.
  The `-v` was essential — Postgres only runs /docker-entrypoint-initdb.d
  against an empty data directory, so without dropping the volume the corrected
  script would never have executed.
- **Prevention:** Seed scripts should be tested at target volume, not at 100 rows.
  A healthcheck alone wouldn't have caught this — the container was gone, not sick.
- **The sentence I'd say in an interview:** "A container vanished from my compose
  stack and I assumed infrastructure. The logs pointed at an integer overflow in
  seed data three layers down — and the fix only took effect once I understood
