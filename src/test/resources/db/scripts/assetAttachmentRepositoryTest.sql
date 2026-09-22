insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, created, updated)
values
    -- Asset in municipality 2281
    ('e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'PRH-0000000001', 'Serveringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2023-01-01', '2033-12-31', '2023-01-01', null),
    -- Another asset in the same municipality, used to verify that attachments do not leak between assets
    ('945576d3-6e92-4118-ba33-53582d338ad3', 'PRH-0000000002', 'Serveringstillstånd', '2281', 'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2023-01-01', '2033-12-31', '2023-01-01', null);

insert into asset_attachment_data (id, file)
values
    (1, 0x255044462d312e340a),
    (2, 0x255044462d312e340a),
    (3, 0x255044462d312e340a),
    (4, 0x255044462d312e340a);

insert into asset_attachment (id, asset_id, asset_attachment_data_id, municipality_id, file_name, mime_type, file_size, category, description, deleted, created, updated)
values
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 1, '2281', 'lokalritning.pdf', 'application/pdf', 9, 'LOKALRITNING', 'Ritning över serveringslokal', false, '2023-01-01 10:00:00', null),
    ('647e3062-62dc-499f-9faa-e54cb97aa214', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 2, '2281', 'planritning.pdf', 'application/pdf', 9, 'PLANRITNING', null, false, '2023-01-02 10:00:00', null),
    ('cba6f0e5-e826-4690-8776-37c69d981a2a', '945576d3-6e92-4118-ba33-53582d338ad3', 3, '2281', 'annan-ritning.pdf', 'application/pdf', 9, 'LOKALRITNING', null, false, null, null),
    -- Soft deleted: hidden from the listing and from both mutating paths, still reachable for download
    ('d1e2f3a4-5b6c-7d8e-9f01-2a3b4c5d6e7f', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 4, '2281', 'raderad.pdf', 'application/pdf', 9, 'LOKALRITNING', null, true, '2023-01-03 10:00:00', null);
