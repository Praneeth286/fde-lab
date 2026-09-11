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
  seed data three layers down — and the fix only took effect once I understood.
  ### Drill 0b — Docker daemon dead, engine unreachable (unplanned, real)
- **Date / time to recovery:** 2026-09-04 · ~40 min
- **Symptom:** `docker compose up` returned 500 on the `_ping` endpoint. Error text
  mentioned API version, which was misleading — nothing was listening behind the pipe.
- **First three commands I ran:**
  1. `docker version` — client printed, no Server section → daemon down, not a client issue
  2. `wsl -l -v` — backend VM `docker-desktop` was Running, so WSL wasn't the fault
  3. checked Docker Desktop settings / logs for dockerd startup failure
- **What I believed vs. what it was:** The error pointed at API version mismatch, which
  sent me looking at client/server compatibility. Actual problem was that dockerd inside
  the Desktop VM wasn't starting at all.
- **Fix:** Stopped debugging Desktop and installed the Docker engine natively inside
  WSL2 Ubuntu instead. Removed a layer rather than repairing it.
- **Prevention:** Fewer layers between me and the runtime. Native engine in WSL2 also
  matches the Linux environment my CI and Kubernetes nodes will run.
- **The sentence I'd say in an interview:** "The error message named API versions, but
  the absent Server block in `docker version` told me the daemon was simply down. After
  confirming the WSL backend was healthy, I judged that repairing Docker Desktop wasn't
  worth the time and switched to a native engine — which was closer to production anyway."
  ### Drill 0c — @Cacheable silently did nothing (unplanned, real)
- **Date / time to recovery:** 2026-09-09 · ~45 min
- **Symptom:** No error, no exception, nothing in the logs to suggest a problem.
  The only signal was `member_lookup_db` appearing twice for two requests with the
  same memberId, when a working cache would log it once.
- **First three commands I ran:**
  1. `docker compose logs claim-api | grep member_lookup_db` — two lines, confirming
     both requests hit the database
  2. `redis-cli KEYS 'members*'` — empty, which ruled out TTL and serialization
     problems: the cache was never populated at all, not populated-then-lost
  3. checked the logger name in the log line to confirm which class was executing
- **What I believed vs. what it was:** I first suspected Redis connectivity or a
  serialization failure. Actual cause was self-invocation: `submit()` called
  `findMember()` on `this`, so the call never crossed the Spring AOP proxy and the
  @Cacheable aspect was never consulted. The annotation was present and ignored.
- **Fix:** Extracted the cached method into its own bean (`MemberLookupService`) so
  the call crosses a proxy boundary. Verified with the log line count and the
  `members::M00000042` key appearing in Redis.
- **Prevention:** Any @Cacheable or @Transactional method needs a test that proves the
  aspect actually fired — a passing unit test on the method body proves nothing,
  because the proxy isn't involved. The same trap silently disables @Transactional.
- **The sentence I'd say in an interview:** "The cache annotation was there and doing
  nothing. Spring's @Cacheable and @Transactional both work through an AOP proxy, and
  an internal call from within the same bean bypasses it entirely — so the aspect is
  silently skipped. The empty Redis keyspace was what told me it had never been
  populated rather than expired."
  ### Drill 0d — Maven built fine in Docker, failed on the host
- **Date / time to recovery:** 2026-09-11 · ~15 min
- **Symptom:** `mvn test` failed with "release version 17 not supported", while the
  identical build inside Docker had been succeeding for days.
- **First three commands I ran:**
  1. `mvn -version` — showed Maven running on a JDK that wasn't 17
  2. `java -version` — different answer, because Maven reads JAVA_HOME, not PATH
  3. `ls /usr/lib/jvm/` — only JDK 25 present; 25 dropped --release 17 support
- **What I believed vs. what it was:** I assumed the Temurin 17 I installed on day 1
  was available. It was installed on **Windows**; Ubuntu had only the JDK apt pulled
  in as a Maven dependency. Two filesystems, two toolchains.
- **Fix:** `apt install openjdk-17-jdk`, then set JAVA_HOME in ~/.bashrc.
- **Prevention:** The Dockerfile pins `maven:3.9-eclipse-temurin-17`, which is why the
  container build never broke. Pinned toolchains in the image are the reason
  "works on my machine" stops being a question.
- **The sentence I'd say in an interview:** "The containerized build passed and the
  host build failed on the same source. The image pins its JDK; my host had whatever
  the package manager chose. That gap is exactly what build containers exist to close."