CREATE TABLE destination_link_accounts (
    id UUID PRIMARY KEY,
    destination_link_id UUID NOT NULL REFERENCES destination_links(id) ON DELETE CASCADE,
    label VARCHAR(80) NOT NULL,
    account_identifier VARCHAR(120) NOT NULL,
    position INTEGER NOT NULL
);
CREATE INDEX destination_link_accounts_link_idx ON destination_link_accounts(destination_link_id);
