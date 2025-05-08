
    create table additional_parameter (
        asset_id varchar(255) not null,
        parameter_key varchar(255) not null,
        parameter_value varchar(255) not null,
        primary key (asset_id, parameter_key)
    ) engine=InnoDB;

    create table asset (
        issued date not null,
        valid_to date,
        created datetime(6),
        updated datetime(6),
        asset_id varchar(255) not null,
        description varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        origin varchar(255),
        party_id varchar(255) not null,
        status_reason varchar(255),
        `type` varchar(255) not null,
        party_type enum ('ENTERPRISE','PRIVATE'),
        status enum ('ACTIVE','BLOCKED','EXPIRED','TEMPORARY'),
        primary key (id)
    ) engine=InnoDB;

    create table case_reference_id (
        asset_id varchar(255) not null,
        case_reference_id varchar(255) not null
    ) engine=InnoDB;

    create table status (
        created datetime(6),
        updated datetime(6),
        municipality_id varchar(255),
        name varchar(255) not null,
        primary key (name)
    ) engine=InnoDB;

    create table status_reason (
        reason varchar(255) not null,
        status_name varchar(255) not null
    ) engine=InnoDB;

    create index idx_additional_parameter_asset_id 
       on additional_parameter (asset_id);

    create index idx_asset_municipality_id 
       on asset (municipality_id);

    alter table if exists asset 
       add constraint uc_asset_asset_id unique (asset_id);

    create index idx_case_reference_id_asset_id 
       on case_reference_id (asset_id);

    create index idx_status_municipality_id 
       on status (municipality_id);

    create index idx_status_reason_status_name 
       on status_reason (status_name);

    alter table if exists additional_parameter 
       add constraint fk_additional_parameter_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists case_reference_id 
       add constraint fk_case_reference_id_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists status_reason 
       add constraint fk_status_reason_status_name 
       foreign key (status_name) 
       references status (name);
