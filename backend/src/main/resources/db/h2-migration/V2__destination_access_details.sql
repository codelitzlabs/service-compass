ALTER TABLE destination_links ADD COLUMN authentication_method VARCHAR(40);
ALTER TABLE destination_links ADD COLUMN account_identifier VARCHAR(120);
ALTER TABLE destination_links ADD COLUMN access_notes VARCHAR(500);
ALTER TABLE destination_links ADD COLUMN access_url VARCHAR(500);
