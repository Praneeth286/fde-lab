-- NOTE: there is deliberately NO index on claims.member_id or members.state.
-- Milestone 1 asks you to find that yourself with EXPLAIN ANALYZE, then fix it.

CREATE TABLE members (
    member_id   VARCHAR(16)  PRIMARY KEY,
    member_name VARCHAR(120) NOT NULL,
    plan        VARCHAR(16)  NOT NULL,
    state       VARCHAR(2)   NOT NULL
);

CREATE TABLE claims (
    id            BIGSERIAL     PRIMARY KEY,
    claim_number  VARCHAR(20)   NOT NULL UNIQUE,
    member_id     VARCHAR(16)   NOT NULL,
    provider_npi  VARCHAR(10)   NOT NULL,
    service_date  DATE          NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
