create index idx_asset_municipality_id_party_id
    on asset (municipality_id, party_id);

create index idx_asset_municipality_id_asset_id
    on asset (municipality_id, asset_id);

create index idx_asset_municipality_id_created
    on asset (municipality_id, created);

create index idx_asset_status_valid_to
    on asset (status, valid_to);

create index idx_additional_parameter_key_value
    on additional_parameter (parameter_key, parameter_value);

-- Covered by the leading column of idx_asset_municipality_id_party_id
drop index idx_asset_municipality_id on asset;

-- Covered by the leading column of the primary key (asset_id, parameter_key)
drop index idx_additional_parameter_asset_id on additional_parameter;
