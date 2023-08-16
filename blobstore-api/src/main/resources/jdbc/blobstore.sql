-- LENGTH bigint not null,

create table BLOB_STORE (
    URI varchar(1000) not null primary key,
    BLOB blob not null
);

alter table BLOB_STORE add constraint BLOB_STORE_CK_URI check(length(trim(URI)) > 0);
--create index BLOB_STORE_IDX_URI on BLOB_STORE (URI);
