ALTER TABLE destination_links
    ADD COLUMN authentication_method VARCHAR(40),
    ADD COLUMN account_identifier VARCHAR(120),
    ADD COLUMN access_notes VARCHAR(500),
    ADD COLUMN access_url VARCHAR(500);
