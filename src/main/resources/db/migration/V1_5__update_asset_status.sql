alter table asset
    modify status enum ('ACTIVE', 'BLOCKED', 'EXPIRED', 'TEMPORARY') null;
