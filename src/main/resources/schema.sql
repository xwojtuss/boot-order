create table if not exists users (
    id bigserial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);
