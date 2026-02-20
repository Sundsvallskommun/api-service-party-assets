insert into status (name, created, municipality_id)
values ('BLOCKED', now(), '2281');

insert into status_reason (status_name, municipality_id, reason)
values ('BLOCKED', '2281', 'IRREGULARITY'),
       ('BLOCKED', '2281', 'LOST');
