insert into asset (id, asset_id, description, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, revision, actor, created, updated)
values
    -- Active asset with history: revisions 0 and 1 in asset_revision, 2 on the row itself
    ('e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'PRH-0000000001', 'Serveringstillstånd', '2281', 'CASEDATA',
     'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'BLOCKED', 'Stöldanmäld', 'PERMIT', '2023-01-01', '2033-12-31',
     2, 'third.actor', '2023-01-01 10:00:00', '2023-04-01 10:00:00'),
    -- Never changed: revision 0, no history rows, and updated is still null
    ('cba6f0e5-e826-4690-8776-37c69d981a2a', 'PRH-0000000002', 'Parkeringstillstånd', '2281', 'CASEDATA',
     'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT', '2023-02-01', '2034-01-31',
     0, null, '2023-02-01 10:00:00', null);

insert into asset_revision (id, asset_id, revision, actor, recorded_at, municipality_id, origin, external_asset_id,
                            party_id, party_type, `type`, issued, valid_to, replaces_id, status, status_reason,
                            description, additional_parameters, case_reference_ids, json_parameters, attachments)
values
    ('a0000000-0000-0000-0000-000000000000', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 0, null,
     '2023-02-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000001', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', null, 'Serveringstillstånd',
     '{}', '[]', '[]', '[]'),
    ('a0000000-0000-0000-0000-000000000001', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 1, 'second.actor',
     '2023-03-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000001', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', 'Förnyad', 'Serveringstillstånd',
     '{"key":"value"}', '[]', '[]', '[]');
