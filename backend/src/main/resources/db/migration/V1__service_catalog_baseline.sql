CREATE TABLE services (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    lifecycle VARCHAR(20) NOT NULL CHECK (lifecycle IN ('experimental', 'production', 'deprecated')),
    repository_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX services_name_lower_uq ON services (LOWER(name));

CREATE TABLE service_tags (
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    tag VARCHAR(40) NOT NULL,
    PRIMARY KEY (service_id, tag)
);

CREATE TABLE service_owners (
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    owner VARCHAR(120) NOT NULL,
    PRIMARY KEY (service_id, owner)
);
CREATE INDEX service_owners_owner_idx ON service_owners(owner);

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(300) NOT NULL DEFAULT ''
);
CREATE UNIQUE INDEX teams_name_lower_uq ON teams (LOWER(name));

CREATE TABLE team_members (
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    member VARCHAR(120) NOT NULL,
    PRIMARY KEY (team_id, member)
);

CREATE TABLE service_teams (
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (service_id, team_id)
);

CREATE TABLE environments (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    color VARCHAR(20) NOT NULL
);
CREATE UNIQUE INDEX environments_name_lower_uq ON environments (LOWER(name));

CREATE TABLE service_destinations (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    label VARCHAR(30) NOT NULL,
    position INTEGER NOT NULL
);
CREATE INDEX service_destinations_service_idx ON service_destinations(service_id);

CREATE TABLE destination_links (
    id UUID PRIMARY KEY,
    destination_id UUID NOT NULL REFERENCES service_destinations(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    environment_id UUID REFERENCES environments(id),
    position INTEGER NOT NULL
);
CREATE INDEX destination_links_destination_idx ON destination_links(destination_id);
CREATE INDEX destination_links_environment_idx ON destination_links(environment_id);

-- Useful starter data for first-time installations.
INSERT INTO teams(id, name, description) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Codelitz Platform', 'Owns shared engineering infrastructure and developer experience');

INSERT INTO team_members(team_id, member) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Leandro'),
    ('10000000-0000-0000-0000-000000000001', 'Platform on-call');

INSERT INTO environments(id, name, color) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Production', 'green');

INSERT INTO services(id, name, description, lifecycle, repository_url, created_at, updated_at) VALUES
    (
        '30000000-0000-0000-0000-000000000001',
        'orders-api',
        'Processes customer orders and coordinates fulfillment.',
        'production',
        'https://github.com/codelitz/orders-api',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO service_owners(service_id, owner) VALUES
    ('30000000-0000-0000-0000-000000000001', 'Orders on-call'),
    ('30000000-0000-0000-0000-000000000001', 'orders@codelitz.dev');

INSERT INTO service_tags(service_id, tag) VALUES
    ('30000000-0000-0000-0000-000000000001', 'java'),
    ('30000000-0000-0000-0000-000000000001', 'critical');

INSERT INTO service_teams(service_id, team_id) VALUES
    ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001');

INSERT INTO service_destinations(id, service_id, name, label, position) VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Grafana', 'grafana', 0),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'Argo CD', 'argocd', 1),
    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'Confluence', 'confluence', 2),
    ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', 'Sentry', 'sentry', 3),
    ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000001', 'Swagger UI', 'swagger', 4);

INSERT INTO destination_links(id, destination_id, url, environment_id, position) VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        'https://grafana.example.com/d/orders-api',
        '20000000-0000-0000-0000-000000000001',
        0
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '40000000-0000-0000-0000-000000000002',
        'https://argocd.example.com/applications/orders-api',
        '20000000-0000-0000-0000-000000000001',
        0
    ),
    (
        '50000000-0000-0000-0000-000000000003',
        '40000000-0000-0000-0000-000000000003',
        'https://codelitz.atlassian.net/wiki/spaces/ORDERS',
        NULL,
        0
    ),
    (
        '50000000-0000-0000-0000-000000000004',
        '40000000-0000-0000-0000-000000000004',
        'https://sentry.example.com/projects/orders-api',
        '20000000-0000-0000-0000-000000000001',
        0
    ),
    (
        '50000000-0000-0000-0000-000000000005',
        '40000000-0000-0000-0000-000000000005',
        'https://orders-api.example.com/swagger-ui.html',
        NULL,
        0
    );
