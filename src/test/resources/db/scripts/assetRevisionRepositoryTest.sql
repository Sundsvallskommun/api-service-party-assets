insert into asset (issued, valid_to, created, updated, revision, actor, asset_id, origin, description, id, party_id,
                   party_type, status, status_reason, `type`, municipality_id)
values
    -- Asset with an unbroken revision chain: 0, 1, 2 in asset_revision and 3 as the current state
    ('2023-01-01', '2033-12-31', '2023-01-01', '2023-04-01', 3, 'third.actor', 'PRH-0000000001', 'CASEDATA',
     'Serveringstillstånd', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010',
     'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'BLOCKED', 'Stöldanmäld', 'PERMIT', '2281'),
    -- Asset whose numbering has a hole: revision 1 was never snapshotted
    ('2023-01-01', '2033-12-31', '2023-01-01', '2023-03-01', 3, null, 'PRH-0000000002', 'CASEDATA',
     'Parkeringstillstånd', 'cba6f0e5-e826-4690-8776-37c69d981a2a',
     'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT', '2281');

insert into asset_revision (id, asset_id, revision, actor, recorded_at, municipality_id, origin, external_asset_id,
                            party_id, party_type, `type`, issued, valid_to, replaces_id, status, status_reason,
                            description, additional_parameters, case_reference_ids, json_parameters, attachments)
values
    -- The chain, inserted out of order on purpose so the ordering in the query is what is actually tested
    ('a0000000-0000-0000-0000-000000000001', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 1, 'second.actor',
     '2023-03-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000001', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', null, 'Serveringstillstånd',
     '{"foo":"bar"}', '[]', '[]', '[]'),
    -- Revision 0 has no actor: the asset predates the X-Sent-By header being recorded
    ('a0000000-0000-0000-0000-000000000000', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 0, null,
     '2023-02-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000001', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', null, 'Serveringstillstånd',
     '{}', '[]', '[]', '[]'),
    ('a0000000-0000-0000-0000-000000000002', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 2, 'third.actor',
     '2023-04-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000001', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'BLOCKED', 'Stöldanmäld', 'Serveringstillstånd',
     '{"foo":"bar"}', '["case-1"]', '[]', '[]'),
    -- 0 and 2 only, so a lookup of revision 1 has to come back empty
    ('b0000000-0000-0000-0000-000000000000', 'cba6f0e5-e826-4690-8776-37c69d981a2a', 0, null,
     '2023-02-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000002', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', null, 'Parkeringstillstånd',
     '{}', '[]', '[]', '[]'),
    ('b0000000-0000-0000-0000-000000000002', 'cba6f0e5-e826-4690-8776-37c69d981a2a', 2, null,
     '2023-03-01 10:00:00', '2281', 'CASEDATA', 'PRH-0000000002', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE',
     'PERMIT', '2023-01-01', '2033-12-31', null, 'ACTIVE', null, 'Parkeringstillstånd',
     '{}', '[]', '[]', '[]');
