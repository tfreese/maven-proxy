-- LENGTH bigint not null,

create table BLOB_STORE (
    URI varchar(1000) not null primary key,
    BLOB blob not null
);

ALTER TABLE BLOB_STORE ADD CONSTRAINT BLOB_STORE_CK_URI CHECK(LENGTH(TRIM(URI)) > 0);
--CREATE INDEX BLOB_STORE_IDX_URI ON BLOB_STORE (URI);
