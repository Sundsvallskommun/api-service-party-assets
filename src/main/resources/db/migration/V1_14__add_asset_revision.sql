    alter table asset
        add column revision integer default 0 not null,
        add column version bigint default 0 not null,
        add column actor varchar(255);

    alter table asset_attachment
        add column deleted bit default false not null;

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
        `type` varchar(255),
        additional_parameters longtext,
        attachments longtext,
        case_reference_ids longtext,
        json_parameters longtext,
        primary key (id)
    ) engine=InnoDB;

    alter table if exists asset_revision
       add constraint uq_asset_revision_asset_id_revision unique (asset_id, revision);

    alter table asset_revision
       add constraint fk_asset_revision_asset_id
       foreign key (asset_id)
       references asset (id)
       on delete cascade;
