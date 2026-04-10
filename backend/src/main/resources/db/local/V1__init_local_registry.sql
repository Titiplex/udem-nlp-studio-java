create table if not exists local_identity
(
    id
    bigint
    primary
    key,
    principal_id
    varchar
(
    255
) not null unique, display_name varchar
(
    255
) not null, public_key_pem clob not null, private_key_encrypted_pem clob not null );

create table if not exists project_registry
(
    project_id
    uuid
    primary
    key,
    name
    varchar
(
    255
) not null, manifest_path varchar
(
    2048
) not null, last_opened_at timestamp with time zone not null, favorite boolean not null );

create table if not exists active_project
(
    id
    bigint
    primary
    key,
    active_project_id
    uuid
    not
    null
);

create table if not exists local_secret
(
    id
    uuid
    primary
    key,
    project_id
    uuid
    not
    null,
    secret_ref
    varchar
(
    255
) not null, secret_json_encrypted clob not null );

create index if not exists idx_local_secret_project_ref on local_secret(project_id, secret_ref);