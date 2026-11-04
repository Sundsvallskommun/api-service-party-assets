insert into asset (id, asset_id, description, title, municipality_id, origin, party_id, party_type, status,
                   status_reason, `type`, issued, valid_to, created, updated)
values ('0e4ab1c4-7d02-4d36-9c35-7a3d1bb0b39e', 'PRH-0000000031', 'Parkeringstillstånd', 'Parkeringstillstånd', '2281',
        'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2025-01-01',
        '2099-12-31', '2025-01-01', null),
       -- Another municipality
       ('2b8f1d36-04c4-4b8e-a5a0-7a3f3e2f6c11', 'PRH-0000000032', 'Parkeringstillstånd', 'Parkeringstillstånd', '2260',
        'CASEDATA', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT', '2025-01-01',
        '2099-12-31', '2025-01-01', null);
