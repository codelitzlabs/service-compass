-- Keep starter data fictional and neutral for public installations.
UPDATE teams
SET name = 'Platform Team',
    description = 'Owns shared engineering infrastructure and developer experience'
WHERE id = '10000000-0000-0000-0000-000000000001';

UPDATE team_members
SET member = 'Platform maintainer'
WHERE team_id = '10000000-0000-0000-0000-000000000001'
  AND member = 'Leandro';

UPDATE services
SET repository_url = 'https://github.com/example/orders-api'
WHERE id = '30000000-0000-0000-0000-000000000001';

UPDATE service_owners
SET owner = 'orders@example.com'
WHERE service_id = '30000000-0000-0000-0000-000000000001'
  AND owner = 'orders@codelitz.dev';

UPDATE destination_links
SET url = 'https://docs.example.com/orders/runbook'
WHERE id = '50000000-0000-0000-0000-000000000003';
