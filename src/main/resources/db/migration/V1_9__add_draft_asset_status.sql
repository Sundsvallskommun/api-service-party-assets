alter table asset
    modify status enum ('ACTIVE', 'DRAFT', 'BLOCKED', 'EXPIRED', 'TEMPORARY') null;
