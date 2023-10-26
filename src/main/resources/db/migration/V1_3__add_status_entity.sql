    create table status (
        created datetime(6),
        updated datetime(6),
        name varchar(255) not null,
        primary key (name)
    ) engine=InnoDB;

    create table status_reason (
        reason varchar(255) not null,
        status_name varchar(255) not null
    ) engine=InnoDB;

    create index idx_status_reason_status_name 
       on status_reason (status_name);

    alter table if exists status_reason 
       add constraint fk_status_reason_status_name 
       foreign key (status_name) 
       references status (name);

    insert into status (name, created) 
    values ('BLOCKED', now());
    
    insert into status_reason (status_name, reason)
    values ('BLOCKED', 'IRREGULARITY'),
           ('BLOCKED', 'LOST');
