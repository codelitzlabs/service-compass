ALTER TABLE destination_link_accounts ADD COLUMN authentication_method VARCHAR(40);
UPDATE destination_link_accounts account
SET authentication_method = COALESCE(
    (SELECT link.authentication_method FROM destination_links link WHERE link.id = account.destination_link_id),
    'Not specified'
);
ALTER TABLE destination_link_accounts ALTER COLUMN authentication_method SET NOT NULL;
