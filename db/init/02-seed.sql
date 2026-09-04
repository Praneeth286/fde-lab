INSERT INTO members (member_id, member_name, plan, state)
SELECT 'M' || LPAD(g::text, 8, '0'),
       'Member ' || g,
       (ARRAY['HMO','PPO','EPO','POS'])[1 + (g % 4)],
       (ARRAY['NJ','NY','PA','CT','MA','TX','CA','FL'])[1 + (g % 8)]
FROM generate_series(1, 20000) g;

INSERT INTO claims (claim_number, member_id, provider_npi, service_date, amount, status)
SELECT 'CLM' || LPAD(g::text, 10, '0'),
       'M' || LPAD((1 + (g % 20000))::text, 8, '0'),
       LPAD(((g::bigint * 7919) % 1000000000)::text, 10, '0'),
       DATE '2024-01-01' + (g % 600),
       ROUND((random() * 5000 + 25)::numeric, 2),
       (ARRAY['SUBMITTED','ADJUDICATED','PAID','DENIED'])[1 + (g % 4)]
FROM generate_series(1, 300000) g;

ANALYZE;
