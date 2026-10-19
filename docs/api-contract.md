# REST API contract

Base path `/api/v1`. JSON only. Errors are RFC 9457 problem details (`application/problem+json`).
Authentication: `Authorization: Bearer <jwt>` obtained from `/auth/login`.

## Route matrix

| Method and path | Access | Story |
|-----------------|--------|-------|
| `GET /pokemon` | public | US01 |
| `GET /pokemon/{id}` | public | US02 |
| `POST /pokemon/{id}/sync` | authenticated | US03 |
| `DELETE /pokemon/{id}/sync` | authenticated | US03 |
| `PUT /pokemon/{id}/extras` | authenticated | US04 |
| `POST /auth/register`, `POST /auth/login` | public | auth |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/health` | public | tooling |

Swagger UI is `/swagger-ui/index.html`. `GET /v3/api-docs` is the OpenAPI document. `docs/openapi.json` is a snapshot of that document; its `servers` url is the host that generated the snapshot.

## US01 — `GET /pokemon?limit=20&offset=0`

`limit`: integer 1–50, default 20. `offset`: integer ≥ 0, default 0. Page links are built from the incoming request URL.

```json
{
  "count": 1351,
  "next": "http://localhost:8080/api/v1/pokemon?limit=20&offset=20",
  "previous": null,
  "results": [
    {
      "id": 1,
      "name": "bulbasaur",
      "sprite": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
      "category": "Seed Pokémon",
      "mass": 6.9,
      "skills": [{ "name": "overgrow", "hidden": false }, { "name": "chlorophyll", "hidden": true }],
      "synced": false
    }
  ]
}
```
`mass` is in kilograms. `sprite` and `category` may be `null`. `synced` is added in P03.
`next` is `null` when `offset + limit >= count`; `previous` is `null` when `offset = 0`, otherwise its offset is `max(0, offset - limit)`.

## US02 — `GET /pokemon/{id}`

`id`: integer ≥ 1 (alternative forms use ids ≥ 10001).

```json
{
  "id": 1, "name": "bulbasaur", "image": "https://.../official-artwork/1.png", "sprite": "https://.../pokemon/1.png",
  "category": "Seed Pokémon", "mass": 6.9,
  "skills": [{ "name": "overgrow", "hidden": false }],
  "stats": [{ "name": "hp", "baseValue": 45 }, { "name": "attack", "baseValue": 49 }],
  "description": "A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.",
  "evolutionChain": { "id": 1, "name": "bulbasaur", "sprite": "https://.../pokemon/1.png",
                      "evolvesTo": [{ "id": 2, "name": "ivysaur", "sprite": "...", "evolvesTo": [ ... ] }] },
  "synced": true,
  "extras": { "localizedName": "Bulbassauro", "region": "Kanto", "tags": ["starter", "grass"] }
}
```
`mass` is in kilograms. `image`, `sprite`, `category` and `description` may be `null`. `extras` is `null` when `synced` is `false`.

## US03 — sync

- `POST /pokemon/{id}/sync` (no body) → `201 Created` the first time, `200 OK` when refreshing; body is the detail representation. `404` if PokéAPI has no such Pokémon, `502` if PokéAPI is unavailable.
- `DELETE /pokemon/{id}/sync` → `204 No Content`; `404` if the Pokémon is not stored.

## US04 — `PUT /pokemon/{id}/extras`

Replaces the extras entirely (idempotent). Body is required, `Content-Type: application/json`, unknown properties are rejected.

```json
{ "localizedName": "Bulbassauro", "region": "Kanto", "tags": ["starter", "grass"] }
```
Rules: `localizedName` and `region` optional, trimmed, blank → `null`, max 100 characters. `tags` optional (absent = empty), max 10, each 1–30 characters, trimmed and lower-cased, no duplicates after normalization.
Response `200 OK`: the detail representation. `404` when the Pokémon is not stored, `400` for malformed JSON, unknown fields or rule violations.

## Auth

`POST /auth/register` `{ "username": "ash", "password": "Pikachu#2026" }` → `201 Created` `{ "id": "…", "username": "ash" }`.
Username: 3–30 characters. The request accepts `[A-Za-z0-9._-]`; it is stored lower-case, so the stored value matches `[a-z0-9._-]`, and is unique (`409` if taken). Password: 8–72 chars, at least one letter and one digit.
`POST /auth/login` same body → `200 OK` `{ "accessToken": "…", "tokenType": "Bearer", "expiresIn": 3600 }`. `expiresIn` is the lifetime in seconds. `401` for bad credentials (same message for unknown user and wrong password).

## Error mapping

| Situation | Status | `title` |
|-----------|--------|---------|
| Invalid pagination, invalid path/query value, malformed JSON, unknown field, bean validation or domain rule violation | 400 | Bad request / specific title; validation failures add `errors: [{field, message}]` |
| Missing or invalid token | 401 | Unauthorized |
| Authenticated but not allowed | 403 | Forbidden |
| Pokémon not found upstream / not stored locally | 404 | Pokémon not found / Pokémon not stored |
| Method not allowed / unsupported media type | 405 / 415 | (Spring defaults as problem details) |
| Username already taken | 409 | Username already taken |
| PokéAPI unavailable | 502 | Catalog unavailable |
| Anything unexpected | 500 | Internal error (generic detail) |
