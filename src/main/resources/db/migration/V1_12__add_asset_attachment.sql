
    create table asset_attachment (
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
        file longblob,
        primary key (id)
    ) engine=InnoDB;

    create index idx_asset_attachment_asset_id_municipality_id 
       on asset_attachment (asset_id, municipality_id);

    alter table if exists asset_attachment 
       add constraint uc_asset_attachment_data_id unique (asset_attachment_data_id);

    alter table if exists asset_attachment 
       add constraint fk_asset_attachment_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists asset_attachment 
       add constraint fk_asset_attachment_data_id 
       foreign key (asset_attachment_data_id) 
       references asset_attachment_data (id);
