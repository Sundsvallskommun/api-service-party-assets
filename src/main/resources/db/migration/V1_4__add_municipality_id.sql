alter table asset
    add column municipality_id varchar(255);

alter table status
    add column municipality_id varchar(255);

create index idx_asset_municipality_id
    on asset (municipality_id);

create index idx_status_municipality_id
    on status (municipality_id);
