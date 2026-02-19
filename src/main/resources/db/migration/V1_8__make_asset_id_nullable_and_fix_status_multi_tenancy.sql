-- Make asset_id nullable and remove unique constraint
ALTER TABLE IF EXISTS asset DROP INDEX IF EXISTS uc_asset_asset_id;
ALTER TABLE IF EXISTS asset MODIFY COLUMN asset_id VARCHAR(255) NULL DEFAULT NULL;

-- Fix status multi-tenancy: change PK to composite (name, municipality_id)

-- Drop existing FK on status_reason
ALTER TABLE IF EXISTS status_reason DROP FOREIGN KEY IF EXISTS fk_status_reason_status_name;

-- Drop existing PK on status
ALTER TABLE IF EXISTS status DROP PRIMARY KEY;

-- Populate NULL municipality_id for existing rows (seed data from V1_3 predates V1_4 which added the column)
UPDATE status SET municipality_id = '2281' WHERE municipality_id IS NULL;

-- Make municipality_id NOT NULL
ALTER TABLE IF EXISTS status MODIFY COLUMN municipality_id VARCHAR(255) NOT NULL;

-- Add composite PK
ALTER TABLE IF EXISTS status ADD PRIMARY KEY (name, municipality_id);

-- Add municipality_id column to status_reason
ALTER TABLE IF EXISTS status_reason ADD COLUMN municipality_id VARCHAR(255);

-- Populate municipality_id in status_reason from status table
UPDATE status_reason sr JOIN status s ON sr.status_name = s.name SET sr.municipality_id = s.municipality_id;

-- Make municipality_id NOT NULL after populating
ALTER TABLE IF EXISTS status_reason MODIFY COLUMN municipality_id VARCHAR(255) NOT NULL;

-- Drop old index
ALTER TABLE IF EXISTS status_reason DROP INDEX IF EXISTS idx_status_reason_status_name;

-- Add composite index
CREATE INDEX idx_status_reason_status_name ON status_reason (status_name, municipality_id);

-- Add composite FK
ALTER TABLE IF EXISTS status_reason ADD CONSTRAINT fk_status_reason_status FOREIGN KEY (status_name, municipality_id) REFERENCES status (name, municipality_id);
