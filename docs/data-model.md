# Data model (PostgreSQL, managed by Flyway)

Hibernate runs with `ddl-auto=validate`; the schema is created only by migrations (`V1__create_schema.sql`, then
`V2__seed_demo_data.sql`). Applied migrations are never edited. Identifiers of Pokémon, species and abilities are the
**PokéAPI ids**, not generated values, which makes sync idempotent. No audit or version columns for now (assumption A11).

```
species ─┐ 1
         └─< pokemon >──< pokemon_ability >── ability
                 │
                 ├──< pokemon_stat
                 └──< pokemon_tag
users
```

| Table | Columns | Notes |
|-------|---------|-------|
| `species` | `id` int PK, `name` varchar(100) not null, `category` varchar(200) null, `description` text null, `evolution_chain` jsonb null | `evolution_chain` holds the simplified tree (`EvolutionNode`); read as a whole, never queried by part |
| `pokemon` | `id` int PK, `name` varchar(100) not null, `sprite_url` varchar(500) null, `image_url` varchar(500) null, `weight_hg` int not null, `species_id` int not null FK → species, **extras:** `localized_name` varchar(100) null, `region` varchar(100) null | `weight_hg >= 0` check |
| `ability` | `id` int PK, `name` varchar(100) not null unique | |
| `pokemon_ability` | `pokemon_id` FK, `ability_id` FK, `hidden` boolean not null; PK(`pokemon_id`, `ability_id`) | `ON DELETE CASCADE` from pokemon |
| `pokemon_stat` | `pokemon_id` FK, `position` smallint, `name` varchar(50), `base_value` int; PK(`pokemon_id`, `position`) | `position` preserves PokéAPI order |
| `pokemon_tag` | `pokemon_id` FK, `tag` varchar(30); PK(`pokemon_id`, `tag`) | tags are lower-case, unique per Pokémon |
| `users` | `id` uuid PK, `username` varchar(30) not null unique (lower-case), `password_hash` varchar(100) not null | BCrypt hash |

Persistence rules:
- JPA entities live only in `infrastructure.persistence` and are never exposed; mappers convert to/from domain records.
- `saveSnapshot` upserts species, pokemon snapshot columns, abilities, stats; it **never** writes `localized_name`, `region` or tags.
- `updateExtras` writes only the extras columns and replaces the tag rows.
- Deleting a Pokémon removes its child rows; shared `species` and `ability` rows stay.
- Seed: a handful (about 12) of fully populated Pokémon with extras, generated once from PokéAPI by a throw-away script kept **outside** the repository; the demo user is created by an idempotent `ApplicationRunner` through the registration use case (no hand-computed hashes in SQL).
