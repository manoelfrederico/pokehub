create table species (
    id integer not null,
    name varchar(100) not null,
    category varchar(200),
    description text,
    evolution_chain jsonb,
    constraint pk_species primary key (id)
);

create table ability (
    id integer not null,
    name varchar(100) not null,
    constraint pk_ability primary key (id),
    constraint uq_ability_name unique (name)
);

create table pokemon (
    id integer not null,
    name varchar(100) not null,
    sprite_url varchar(500),
    image_url varchar(500),
    weight_hg integer not null,
    species_id integer not null,
    localized_name varchar(100),
    region varchar(100),
    constraint pk_pokemon primary key (id),
    constraint fk_pokemon_species foreign key (species_id) references species (id),
    constraint ck_pokemon_weight_hg check (weight_hg >= 0)
);

create table pokemon_ability (
    pokemon_id integer not null,
    ability_id integer not null,
    hidden boolean not null,
    constraint pk_pokemon_ability primary key (pokemon_id, ability_id),
    constraint fk_pokemon_ability_pokemon foreign key (pokemon_id) references pokemon (id) on delete cascade,
    constraint fk_pokemon_ability_ability foreign key (ability_id) references ability (id)
);

create table pokemon_stat (
    pokemon_id integer not null,
    position smallint not null,
    name varchar(50) not null,
    base_value integer not null,
    constraint pk_pokemon_stat primary key (pokemon_id, position),
    constraint fk_pokemon_stat_pokemon foreign key (pokemon_id) references pokemon (id) on delete cascade
);

create table pokemon_tag (
    pokemon_id integer not null,
    tag varchar(30) not null,
    constraint pk_pokemon_tag primary key (pokemon_id, tag),
    constraint fk_pokemon_tag_pokemon foreign key (pokemon_id) references pokemon (id) on delete cascade
);

create table users (
    id uuid not null,
    username varchar(30) not null,
    password_hash varchar(100) not null,
    constraint pk_users primary key (id),
    constraint uq_users_username unique (username)
);
