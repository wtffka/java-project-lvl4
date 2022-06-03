create table url (
                     id                            bigint generated by default as identity not null,
                     name                          varchar(255),
                     created_at                    timestamptz not null,
                     PRIMARY KEY (ID),
                     UNIQUE (name)
);