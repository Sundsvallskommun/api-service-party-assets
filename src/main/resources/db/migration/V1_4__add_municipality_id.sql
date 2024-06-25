alter table asset
    add column municipality_id varchar(255);

alter table status
    add column municipality_id varchar(255);
