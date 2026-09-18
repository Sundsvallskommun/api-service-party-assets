insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, created, updated)
values
    -- Active asset, attachments may be added and removed
    ('e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'PRH-0000000001', 'Serveringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2023-01-01', '2033-12-31', '2023-01-01', null),
    -- Expired asset, attachments are read only
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'PRH-0000000002', 'Serveringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'EXPIRED', null, 'PERMIT', '2022-01-01', '2022-12-31', '2022-01-01', '2023-01-01');

insert into asset_attachment_data (id, file)
values
    (1, 0x255044462d312e340a);

insert into asset_attachment (id, asset_id, asset_attachment_data_id, municipality_id, file_name, mime_type, file_size, category, description, created, updated)
values
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', '5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 1, '2281', 'gammal-ritning.pdf', 'application/pdf', 9, 'LOKALRITNING', 'Ritning över serveringslokal', '2023-01-01 10:00:00', null);
