insert into status (name, created)
values ('BLOCKED', now());

insert into status_reason (status_name, reason)
values ('BLOCKED', 'IRREGULARITY'),
       ('BLOCKED', 'LOST');