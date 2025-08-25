    create table if not exists asset_json_parameter (
        asset_id varchar(255) not null,
        id varchar(255) not null,
        parameter_key varchar(255),
        schema_id varchar(255) not null,
        parameter_value longtext,
        primary key (id)
    ) engine=InnoDB;
    
    create table if not exists json_schema (
        created datetime(6),
        municipality_id varchar(8),
        version varchar(32),
        name varchar(64),
        id varchar(255) not null,
        description longtext,
        value longtext,
        primary key (id)
    ) engine=InnoDB;
    
    alter table if exists json_schema 
       add constraint uc_json_schema_municipality_id_name_version unique (municipality_id, name, version);

    alter table if exists asset_json_parameter 
       add constraint fk_asset_json_parameter_asset_id 
       foreign key (asset_id) 
       references asset (id);

    alter table if exists asset_json_parameter 
       add constraint fk_asset_json_parameter_schema_id 
       foreign key (schema_id) 
       references json_schema (id);
       