create table credentials
(
    id           serial primary key auto_increment,
    username     varchar(32),
    password     varchar(255),
    phone_number bigint(8)
);

insert into credentials(username, password, phone_number)
values ('test', 'test', 1);
