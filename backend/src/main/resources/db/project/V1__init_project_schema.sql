create table if not exists workspace_entries
(
    id
    uuid
    primary
    key,
    document_order
    integer
    not
    null,
    raw_chuj_text
    text,
    raw_gloss_text
    text,
    translation
    text,
    corrected_chuj_text
    text,
    corrected_gloss_text
    text,
    corrected_translation
    text,
    approved
    boolean
    not
    null
    default
    false,
    conllu_preview
    text,
    version
    bigint
    not
    null
    default
    0,
    updated_by
    varchar
(
    255
), updated_at timestamp with time zone );

create index if not exists idx_workspace_entries_document_order on workspace_entries(document_order, id);

create table if not exists rules
(
    id
    uuid
    primary
    key,
    name
    varchar
(
    200
) not null, kind varchar
(
    30
) not null, subtype varchar
(
    100
) not null, scope varchar
(
    50
) not null, enabled boolean not null, priority integer not null, description text, payload_json text, raw_yaml text, version bigint not null default 0, updated_by varchar
(
    255
), updated_at timestamp with time zone );

create index if not exists idx_rules_kind_priority_name on rules(kind, priority, name);

create table if not exists annotation_settings
(
    id
    bigint
    primary
    key,
    pos_definitions_yaml
    text,
    feat_definitions_yaml
    text,
    lexicons_yaml
    text,
    extractors_yaml
    text,
    gloss_map_yaml
    text,
    version
    bigint
    not
    null
    default
    0,
    updated_by
    varchar
(
    255
), updated_at timestamp with time zone );

insert into annotation_settings (id,
                                 pos_definitions_yaml,
                                 feat_definitions_yaml,
                                 lexicons_yaml,
                                 extractors_yaml,
                                 gloss_map_yaml,
                                 version,
                                 updated_by,
                                 updated_at)
select 1,
       '',
       '',
       '',
       '',
       '',
       0,
       'system',
       now() where not exists (
    select 1 from annotation_settings where id = 1
);