
	insert into asset (issued, valid_to, created, updated, asset_id, description, id, party_id, status, status_reason, `type`)
	values
	-- Citizen 1
	('2022-01-01', '2022-12-31', '2022-01-01', '2023-01-01', 'PRH-0000000001', 'Parkeringstillstånd', '5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'EXPIRED', null, 'PERMIT'),
	('2023-01-01', '2023-12-31', '2023-01-01', '2023-06-01', 'PRH-0000000002', 'Parkeringstillstånd', '945576d3-6e92-4118-ba33-53582d338ad3', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'BLOCKED', 'Stöldanmäld', 'PERMIT'),
	('2023-01-01', '2023-12-31', '2023-01-01', null, 'CON-0000000003', 'Bygglov', 'e84b72ee-1a34-44b5-b8f6-2e0e42e99010', 'f2ef7992-7b01-4185-a7f8-cf97dc7f438f', 'ACTIVE', null, 'PERMIT'),
	-- Citizen 2
	('2022-02-01', '2023-01-31', '2022-02-01', '2023-01-01', 'PRH-0000000011', 'Parkeringstillstånd', '7c145278-da81-49b0-a011-0f8f6821e3a0', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'EXPIRED', null, 'PERMIT'),
	('2023-02-01', '2024-01-31', '2023-02-01', null, 'PRH-0000000012', 'Parkeringstillstånd', 'cba6f0e5-e826-4690-8776-37c69d981a2a', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ACTIVE', null, 'PERMIT'),
	('2023-02-01', '2024-01-31', '2023-02-01', null, 'CON-0000000013', 'Bygglov', '647e3062-62dc-499f-9faa-e54cb97aa214', 'c5d21b57-c785-4d3c-8361-940cae999ff7', 'ACTIVE', null, 'PERMIT');

	insert into additional_parameter (asset_id, parameter_key, parameter_value) 
	values
	-- Citizen 1, PRH-1
	('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'first_key', 'first_value'),
	('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'second_key', 'second_value'),
	-- Citizen 1, PRH-2
	('945576d3-6e92-4118-ba33-53582d338ad3', 'first_key', 'third_value'),
	-- Citizen 2, PRH-11
	('7c145278-da81-49b0-a011-0f8f6821e3a0', 'some_key', 'some_value'),
	-- Citizen 2, PRH-12
	('cba6f0e5-e826-4690-8776-37c69d981a2a', 'other_key', 'other_value');
	
    insert into case_reference_id (asset_id, case_reference_id)
    values
	-- Citizen 1, PRH-1
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'case_reference_1'),
    ('5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884', 'case_reference_2'),
	-- Citizen 1, PRH-2
    ('945576d3-6e92-4118-ba33-53582d338ad3', 'case_reference_3'),
	-- Citizen 2, PRH-11
    ('7c145278-da81-49b0-a011-0f8f6821e3a0', 'case_reference_4');    
    

