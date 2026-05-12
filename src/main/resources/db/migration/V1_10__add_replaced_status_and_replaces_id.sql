alter table asset
    modify status enum ('ACTIVE', 'BLOCKED', 'DRAFT', 'EXPIRED', 'REPLACED', 'TEMPORARY') null;

alter table asset
    add column replaces_id varchar(255) null;
