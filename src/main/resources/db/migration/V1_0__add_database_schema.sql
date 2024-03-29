
    create table additional_parameter (
        asset_id varchar(255) not null,
        parameter_key varchar(255) not null,
        parameter_value varchar(255) not null,
        primary key (asset_id, parameter_key)
    ) engine=InnoDB;

    create table asset (
        issued date,
        valid_to date,
        created datetime(6),
        updated datetime(6),
        asset_id varchar(255),
        description varchar(255),
        id varchar(255) not null,
        party_id varchar(255),
        status enum ('ACTIVE','BLOCKED','EXPIRED'),
        `type` varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table case_reference_id (
        asset_id varchar(255) not null,
        case_reference_id varchar(255) not null
    ) engine=InnoDB;

    create index idx_additional_parameter_asset_id 
       on additional_parameter (asset_id);

    alter table if exists asset 
       add constraint uc_asset_asset_id unique (asset_id);

    create index idx_case_reference_id_asset_id 
       on case_reference_id (asset_id);

    alter table if exists additional_parameter 
       add constraint fk_additional_parameter_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists case_reference_id 
       add constraint fk_case_reference_id_asset_id 
       foreign key (asset_id) 
       references asset (id);
