
    create table User (
       id bigint generated by default as identity,
        email varchar(255) not null,
        password varchar(255) not null,
        primary key (id)
    );

    alter table User 
       add constraint UK_e6gkqunxajvyxl5uctpl2vl2p unique (email);
