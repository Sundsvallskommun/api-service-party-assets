-- Drop foreign key constraint from asset_json_parameter
ALTER TABLE asset_json_parameter
    DROP FOREIGN KEY fk_asset_json_parameter_schema_id;

-- Drop the json_schema table
DROP TABLE IF EXISTS json_schema;
