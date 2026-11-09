-- In alphabetical order as Hibernate generates it, since MariaDB sorts an enum column by the position of its values
alter table asset
    modify status enum ('ACTIVE', 'BLOCKED', 'DRAFT', 'EXPIRED', 'REPLACED', 'REVOKED', 'TEMPORARY') null;
