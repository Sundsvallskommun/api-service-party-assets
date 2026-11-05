drop table if exists case_reference_id;

alter table asset_revision
    drop column case_reference_ids;
