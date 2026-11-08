-- Backfill the optimistic-locking column for databases created before @Version was added.
ALTER TABLE event ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE event SET version = 0 WHERE version IS NULL;
ALTER TABLE event ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE event ALTER COLUMN version SET NOT NULL;
