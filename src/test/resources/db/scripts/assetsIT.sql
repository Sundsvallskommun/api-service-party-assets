insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, created, updated)
values
    -- Private party
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'PRH-0000000001', 'Parkeringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'EXPIRED', null, 'PERMIT', '2022-01-01', '2022-12-31', '2022-01-01', '2023-01-01'),
    ('945576d3-6e92-4118-ba33-53582d338ad3', 'PRH-0000000002', 'Parkeringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'BLOCKED', 'Stöldanmäld', 'PERMIT', '2023-01-01', '2023-12-31', '2023-01-01', '2023-06-01'),
    ('e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'CON-0000000003', 'Bygglov', '2281', 'MAZEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2023-01-01', '2023-12-31', '2023-01-01', null),
    -- Private party -- draft
    ('1bdbb931-5c6f-4ffe-bfc9-d9e5bffe48a4', 'PRH-0000000003', 'Parkeringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'DRAFT', null, 'PERMIT', '2024-01-01', '2024-12-31', '2024-01-01', '2024-06-01'),
    ('abd6596f-45a0-4912-89e4-8cdcea9a043a', 'PRH-0000000004', 'Parkeringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'DRAFT', null, 'PERMIT', '2025-01-01', '2025-12-31', '2025-01-01', '2025-06-01'),
    -- Enterprise party
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', 'PRH-0000000011', 'Parkeringstillstånd', '2281', 'CASEDATA', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'EXPIRED', null, 'PERMIT', '2022-02-01', '2023-01-31', '2022-02-01', '2023-01-01'),
    ('cba6f0e5-e826-4690-8776-37c69d981a2a', 'PRH-0000000012', 'Parkeringstillstånd', '2281', 'CASEDATA', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT', '2023-02-01', '2024-01-31', '2023-02-01', null),
    ('647e3062-62dc-499f-9faa-e54cb97aa214', 'CON-0000000013', 'Bygglov', '2281', 'MAZEDATA', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT', '2023-02-01', '2024-01-31', '2023-02-01', null);

insert into additional_parameter (asset_id, parameter_key, parameter_value)
values
    -- Private party, PRH-1
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'first_key', 'first_value'),
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'second_key', 'second_value'),
    -- Private party, PRH-2
    ('945576d3-6e92-4118-ba33-53582d338ad3', 'first_key', 'third_value'),
    -- Private party, PRH-3 (draft)
    ('1bdbb931-5c6f-4ffe-bfc9-d9e5bffe48a4', 'first_key', 'first_value'),
    -- Private party, PRH-4 (draft)
    ('abd6596f-45a0-4912-89e4-8cdcea9a043a', 'first_key', 'first_value'),
    ('abd6596f-45a0-4912-89e4-8cdcea9a043a', 'second_key', 'second_value'),
    -- Enterprise party, PRH-11
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', 'some_key', 'some_value'),
    -- Enterprise party, PRH-12
    ('cba6f0e5-e826-4690-8776-37c69d981a2a', 'other_key', 'other_value');

insert into `status` (name, created, municipality_id)
values ('BLOCKED', now(), '2281');

insert into status_reason (status_name, municipality_id, reason)
values ('BLOCKED', '2281', 'IRREGULARITY'),
       ('BLOCKED', '2281', 'LOST');

insert into asset_json_parameter
    (id, asset_id, parameter_key, parameter_value, schema_id)
values
    ('d401c0b0-14a6-4cee-ab20-786e57691928', '5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'theKey', '{"productId":666, "productName":"A product name", "price":42}', '2281_schema_1.0');

-- Draft activation test data (unique partyId, does not affect other tests)
insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, replaces_id, created, updated)
values
    ('a0000000-0000-0000-0000-000000000001', 'CON-TEST-0001', 'Test asset', '2281', 'TESTDATA', 'a0000000-0000-0000-0000-000000000099', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2026-01-01', null, null, '2026-01-01', null),
    ('a0000000-0000-0000-0000-000000000002', 'CON-TEST-0001', 'Test asset', '2281', 'TESTDATA', 'a0000000-0000-0000-0000-000000000099', 'PRIVATE', 'DRAFT', null, 'PERMIT', '2026-01-01', null, 'a0000000-0000-0000-0000-000000000001', '2026-01-01', null);

-- Alcohol serving and tobacco sales permits for the search tests (parties of their own, does not affect other tests)
insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, title, created, updated)
values
    ('b1000000-0000-0000-0000-000000000001', 'AOT-0000000001', 'Serveringstillstånd', '2281', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000091', 'ENTERPRISE', 'ACTIVE', null, 'AlcoholServingPermit', '2025-05-01', null, 'Stadigvarande tillstånd för servering av alkohol', '2025-05-01 10:00:00', null),
    ('b1000000-0000-0000-0000-000000000002', 'AOT-0000000002', 'Serveringstillstånd', '2281', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000092', 'ENTERPRISE', 'ACTIVE', null, 'AlcoholServingPermit', '2025-06-01', '2026-08-31', 'Tillfälligt tillstånd för servering av alkohol', '2025-06-01 10:00:00', '2025-06-15 08:30:00'),
    ('b1000000-0000-0000-0000-000000000003', 'AOT-0000000003', 'Serveringstillstånd', '2281', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000093', 'ENTERPRISE', 'EXPIRED', null, 'AlcoholServingPermit', '2024-03-01', '2024-09-30', 'Tillfälligt tillstånd för servering av alkohol', '2024-03-01 10:00:00', '2024-10-01 00:00:00'),
    ('b1000000-0000-0000-0000-000000000004', 'AOT-0000000004', 'Serveringstillstånd', '2281', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000091', 'ENTERPRISE', 'DRAFT', null, 'AlcoholServingPermit', '2026-01-01', null, 'Stadigvarande tillstånd för servering av alkohol', '2026-01-02 10:00:00', null),
    ('b1000000-0000-0000-0000-000000000005', 'AOT-0000000005', 'Tobakstillstånd', '2281', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000091', 'ENTERPRISE', 'ACTIVE', null, 'TobaccoSalesPermit', '2025-05-01', null, 'Tillstånd för försäljning av tobak', '2025-05-01 11:00:00', null),
    -- Another municipality
    ('b1000000-0000-0000-0000-000000000006', 'AOT-0000000006', 'Serveringstillstånd', '2262', 'SUPPORTMANAGEMENT', 'b1000000-0000-0000-0000-000000000094', 'ENTERPRISE', 'ACTIVE', null, 'AlcoholServingPermit', '2025-05-01', null, 'Stadigvarande tillstånd för servering av alkohol', '2025-05-01 09:00:00', null);

insert into additional_parameter (asset_id, parameter_key, parameter_value)
values
    ('b1000000-0000-0000-0000-000000000001', 'errandId', '2a4c6e80-1b3d-4f5a-8c7e-9d0f1a2b3c4d'),
    ('b1000000-0000-0000-0000-000000000002', 'errandId', '7e1d3f5a-2b4c-4d6e-8f0a-1b2c3d4e5f6a'),
    ('b1000000-0000-0000-0000-000000000005', 'errandId', '2a4c6e80-1b3d-4f5a-8c7e-9d0f1a2b3c4d');

insert into asset_json_parameter (id, asset_id, parameter_key, parameter_value, schema_id)
values
    ('b1000000-0000-0000-0000-000000000101', 'b1000000-0000-0000-0000-000000000001', 'aot_alcohol_party_asset', '{"beverages":["WINE","STRONG_BEER"],"additionalScopes":["OUTDOOR_SERVING"]}', '2281_aot_alcohol_party_asset_0.1');
