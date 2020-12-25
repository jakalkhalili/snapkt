create table tokens
(
    id              serial primary key auto_increment,
    credentials_id  long,
    value           varchar(255),
    creation_date   datetime,
    expiration_date datetime
);
