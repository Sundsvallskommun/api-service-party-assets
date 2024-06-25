insert into status (name, created, municipality_id)
values ('BLOCKED', now(), '2281');

insert into status_reason (status_name, reason)
values ('BLOCKED', 'IRREGULARITY'),
       ('BLOCKED', 'LOST');
