
    create table additional_parameter (
        asset_id varchar(255) not null,
        parameter_key varchar(255) not null,
        parameter_value varchar(255) not null,
        primary key (asset_id, parameter_key)
    ) engine=InnoDB;

    create table asset (
        issued date not null,
        revision integer default 0 not null,
        valid_to date,
        created datetime(6),
        updated datetime(6),
        version bigint default 0 not null,
        actor varchar(255),
        asset_id varchar(255),
        description varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        origin varchar(255),
        party_id varchar(255) not null,
        replaces_id varchar(255),
        status_reason varchar(255),
        title varchar(255),
        `type` varchar(255) not null,
        party_type enum ('ENTERPRISE','PRIVATE'),
        status enum ('ACTIVE','BLOCKED','DRAFT','EXPIRED','REPLACED','TEMPORARY'),
        primary key (id)
    ) engine=InnoDB;

    create table asset_attachment (
        deleted bit default false not null,
        file_size integer,
        asset_attachment_data_id bigint not null,
        created datetime(6),
        municipality_id varchar(8) not null,
        updated datetime(6),
        asset_id varchar(255) not null,
        category varchar(255),
        description varchar(255),
        file_name varchar(255),
        id varchar(255) not null,
        mime_type varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table asset_attachment_data (
        id bigint not null auto_increment,
        file longblob not null,
        primary key (id)
    ) engine=InnoDB;

    create table asset_json_parameter (
        asset_id varchar(255) not null,
        id varchar(255) not null,
        parameter_key varchar(255),
        schema_id varchar(255) not null,
        parameter_value longtext,
        primary key (id)
    ) engine=InnoDB;

    create table asset_revision (
        issued date,
        revision integer not null,
        valid_to date,
        recorded_at datetime(6) not null,
        party_type varchar(32),
        status varchar(32),
        actor varchar(255),
        asset_id varchar(255) not null,
        description varchar(255),
        external_asset_id varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        origin varchar(255),
        party_id varchar(255),
        replaces_id varchar(255),
        status_reason varchar(255),
        title varchar(255),
        `type` varchar(255),
        additional_parameters longtext,
        attachments longtext,
        case_reference_ids longtext,
        json_parameters longtext,
        primary key (id)
    ) engine=InnoDB;

    create table case_reference_id (
        asset_id varchar(255) not null,
        case_reference_id varchar(255) not null
    ) engine=InnoDB;

    create table status (
        created datetime(6),
        updated datetime(6),
        municipality_id varchar(255) not null,
        name varchar(255) not null,
        primary key (municipality_id, name)
    ) engine=InnoDB;

    create table status_reason (
        municipality_id varchar(255) not null,
        reason varchar(255) not null,
        status_name varchar(255) not null
    ) engine=InnoDB;

    create index idx_additional_parameter_asset_id 
       on additional_parameter (asset_id);

    create index idx_asset_municipality_id 
       on asset (municipality_id);

    create index idx_asset_attachment_asset_id_municipality_id 
       on asset_attachment (asset_id, municipality_id);

    alter table if exists asset_attachment 
       add constraint uc_asset_attachment_data_id unique (asset_attachment_data_id);

    alter table if exists asset_revision 
       add constraint uq_asset_revision_asset_id_revision unique (asset_id, revision);

    create index idx_case_reference_id_asset_id 
       on case_reference_id (asset_id);

    create index idx_status_reason_status_name 
       on status_reason (status_name, municipality_id);

    alter table if exists additional_parameter 
       add constraint fk_additional_parameter_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists asset_attachment 
       add constraint fk_asset_attachment_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists asset_attachment 
       add constraint fk_asset_attachment_data_id 
       foreign key (asset_attachment_data_id) 
       references asset_attachment_data (id);

    alter table if exists asset_json_parameter 
       add constraint fk_asset_json_parameter_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists case_reference_id 
       add constraint fk_case_reference_id_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists status_reason 
       add constraint fk_status_reason_status 
       foreign key (municipality_id, status_name) 
       references status (municipality_id, name);
