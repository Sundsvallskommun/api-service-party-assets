
	insert into asset (issued, valid_to, created, updated, asset_id, origin, description, id, party_id, party_type, status, status_reason, `type`)
	values
	-- Private party
	('2022-01-01', '2022-12-31', '2022-01-01', '2023-01-01', 'PRH-0000000001', 'CASEDATA', 'Parkeringstillstånd', '5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'EXPIRED', null, 'PERMIT'),
	('2023-01-01', '2023-12-31', '2023-01-01', '2023-06-01', 'PRH-0000000002', 'CASEDATA', 'Parkeringstillstånd', '945576d3-6e92-4118-ba33-53582d338ad3', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'BLOCKED', 'Stöldanmäld', 'PERMIT'),
	('2023-01-01', '2023-12-31', '2023-01-01', null, 'CON-0000000003', 'MAZEDATA', 'Bygglov', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'PRIVATE', 'ACTIVE', null, 'PERMIT'),
	-- Enterprise party
	('2022-02-01', '2023-01-31', '2022-02-01', '2023-01-01', 'PRH-0000000011', 'CASEDATA', 'Parkeringstillstånd', '7c145278-da81-49b0-a011-0f8f6821e3a0', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'EXPIRED', null, 'PERMIT'),
	('2023-02-01', '2024-01-31', '2023-02-01', null, 'PRH-0000000012', 'CASEDATA', 'Parkeringstillstånd', 'cba6f0e5-e826-4690-8776-37c69d981a2a', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT'),
	('2023-02-01', '2024-01-31', '2023-02-01', null, 'CON-0000000013', 'MAZEDATA', 'Bygglov', '647e3062-62dc-499f-9faa-e54cb97aa214', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ENTERPRISE', 'ACTIVE', null, 'PERMIT');

	insert into additional_parameter (asset_id, parameter_key, parameter_value) 
	values
	-- Private party, PRH-1
	('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'first_key', 'first_value'),
	('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'second_key', 'second_value'),
	-- Private party, PRH-2
	('945576d3-6e92-4118-ba33-53582d338ad3', 'first_key', 'third_value'),
	-- Enterprise party, PRH-11
	('7c145278-da81-49b0-a011-0f8f6821e3a0', 'some_key', 'some_value'),
	-- Enterprise party, PRH-12
	('cba6f0e5-e826-4690-8776-37c69d981a2a', 'other_key', 'other_value');
	
    insert into case_reference_id (asset_id, case_reference_id)
    values
	-- Private party, PRH-1
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'case_reference_1'),
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'case_reference_2'),
	-- Private party, PRH-2
    ('945576d3-6e92-4118-ba33-53582d338ad3', 'case_reference_3'),
	-- Enterprise party, PRH-11
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', 'case_reference_4');
    
    insert into `status` (name, created)
    values ('BLOCKED', now());

    insert into status_reason (status_name, reason)
    values ('BLOCKED', 'IRREGULARITY'),
           ('BLOCKED', 'LOST');
