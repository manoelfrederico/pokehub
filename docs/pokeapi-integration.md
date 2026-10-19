# PokéAPI integration

Base URL: `https://pokeapi.co/api/v2` (REST, no authentication, read-only, JSON). Documentation: https://pokeapi.co/docs/v2

## Fair use
Cache every resource locally and avoid unnecessary calls; abusive clients can be blocked. The service therefore caches
all responses, caps concurrent outbound requests, and sends a descriptive `User-Agent`. Tests never hit the network.

## Endpoints and the fields we read

| Endpoint | Used for | Fields read |
|----------|----------|-------------|
| `GET /pokemon?limit={n}&offset={m}` | US01 page of references | `count`, `results[].name`, `results[].url` (id = last path segment). Ignore `next`/`previous`: they are absolute PokéAPI URLs. |
| `GET /pokemon/{id}` | US01, US02 | `id`, `name`, `weight` (hectograms), `abilities[].ability.name`, `abilities[].ability.url` (id = last path segment), `abilities[].is_hidden`, `stats[].base_stat`, `stats[].stat.name`, `sprites.front_default`, `sprites.other["official-artwork"].front_default`, `sprites.other.home.front_default`, `species.url` |
| `GET /pokemon-species/{id}` | US01 category, US02 description + lineage, US03 species name | `name`, `genera[]` (`genus`, `language.name`), `flavor_text_entries[]` (`flavor_text`, `language.name`), `evolution_chain.url` |
| `GET /evolution-chain/{id}` | US02 lineage | `chain` → recursive `{ species{name,url}, evolves_to[] }` |

Rules:
- The species id comes from `pokemon.species.url`, **never** from the Pokémon id. The catalog has 1351 entries (`count`)
  because alternative forms (ids ≥ 10001, e.g. mega evolutions) are included and map to their base species.
- Species and chain URLs are used only to extract the numeric id; the request is built from the configured base URL.
- Unknown fields are ignored. Some fields are `null` (e.g. `past_abilities[].abilities[].ability`); do not model what is not read.

## Mapping rules (anti-corruption layer)

- **Mass:** `weight` hectograms → `Mass`; kilograms = hectograms / 10 (`BigDecimal`, scale 1).
- **Sprite for listings:** first non-blank of `front_default` → `official-artwork.front_default` → `home.front_default`, else `null`.
- **Image for detail:** first non-blank of `official-artwork.front_default` → `front_default` → `home.front_default`, else `null`.
- **Category:** the `genus` whose `language.name == "en"`; `null` if absent.
- **Description:** English `flavor_text_entries`; the **last** English entry (most recent game) with every run of whitespace
  (including `\n` and form feed `\f`) collapsed to a single space and trimmed; `null` if none.
- **Stats:** keep PokéAPI order and names (`hp`, `attack`, `defense`, `special-attack`, `special-defense`, `speed`).
- **Evolution lineage:** recursive tree `EvolutionNode(id, name, spriteUrl, evolvesTo)`. Single-stage Pokémon yield a root
  without children; branching (e.g. Eevee) yields several children. Node `spriteUrl` follows the convention
  `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id}.png` (documented trade-off: no extra call per node).

## Failure mapping

| Upstream situation | Result in the application |
|--------------------|---------------------------|
| 404 on `/pokemon/{id}` requested by the user | `PokemonNotFoundException` → HTTP 404 |
| Any other 4xx/5xx, timeout, I/O error, empty or malformed body, malformed embedded URL | `CatalogUnavailableException` → HTTP 502 |

## Test fixtures
Real trimmed payloads live in `backend/src/test/resources/pokeapi/` (`pokemon-1.json`, `species-1.json`, `list-first-page.json`,
`pokemon-10033.json` for an alternative form, `evolution-chain-1.json`, plus a branching chain such as Eevee's). They keep
unrelated noise (including `null` oddities) on purpose to prove tolerant parsing.
