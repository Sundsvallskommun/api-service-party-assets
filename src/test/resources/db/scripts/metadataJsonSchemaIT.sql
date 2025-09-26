insert into json_schema 
     (id, created, municipality_id, version, name, description, value) 
values 
     ('2281_schema_1.0.0', '2025-01-01 12:13:14.000', '2281', '1.0.0', 'schema', 'Schema 1', '{ "type": "object", "properties": { "firstName": { "type": "string" }, "lastName": { "type": "string" } } }'),
     ('2281_schema_1.5.0', '2025-02-02 12:13:14.000', '2281', '1.5.0', 'schema', 'Schema 1', '{ "type": "object", "properties": { "firstName": { "type": "string" }, "lastName": { "type": "string" } } }'),
     ('2281_schema_with_references_1.0.0', '2025-02-02 12:13:14.000', '2281', '1.0.0', 'schema_with_references', 'Schema 2', '{ "type": "object", "properties": { "firstName": { "type": "string" }, "lastName": { "type": "string" } } }');

-- Create an asset that references one of the schemas: '2281_schema_with_references_1.0.0'
insert into asset 
     (issued, valid_to, created, updated, asset_id, origin, description, id, party_id, party_type, status, status_reason, `type`, municipality_id)
values
     ('2022-01-01', '2022-12-31', '2022-01-01', '2023-01-01', 'PRH-0000000001', 'CASEDATA', 'Parkeringstillstånd', 'e9f5d96b-4339-4589-9cf0-a2b6c5fcbbd3','f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'EXPIRED', null, 'PERMIT', '2281');

insert into asset_json_parameter 
    (id, asset_id, parameter_key, parameter_value, schema_id)
values
    ('d401c0b0-14a6-4cee-ab20-786e57691928', 'e9f5d96b-4339-4589-9cf0-a2b6c5fcbbd3', 'first_key', '{"firstName":"John", "lastName":"Doe"}', '2281_schema_with_references_1.0.0');