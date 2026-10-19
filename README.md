# Pokehub

Pokehub browses Pokémon from [PokéAPI](https://pokeapi.co/), stores a local copy of the ones you choose, and lets a signed-in user add a localized name, a region and tags. Reads are public. Sync, unsync and editing require a JWT.

**Screenshot:** placeholder. Before you submit, add a PNG or a short GIF of the list and a synced detail page at `docs/images/pokehub.png` and drop it in here.

<!-- SCREENSHOT: replace the paragraph above with:
     ![Pokehub](docs/images/pokehub.png)
-->

## Stack

| Part | Choice |
|------|--------|
| API | Java 25, Spring Boot 4.1, Gradle |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| Security | Spring Security, JWT (HS256), BCrypt |
| External API | PokéAPI via `RestClient`, Caffeine cache |
| API docs | springdoc-openapi |
| Front end | React 19, TypeScript, Vite, Tailwind CSS, TanStack Query, React Router |
| Tests | JUnit 5, AssertJ, Mockito, MockMvc, ArchUnit, JaCoCo; Vitest, Testing Library |
| Delivery | Docker Compose (Postgres, API, nginx) |

## Architecture

Clean Architecture. Business rules sit in `domain` and `application` and do not import Spring, Jackson or JPA. `web` does not import `infrastructure`. `ArchitectureTest` enforces that.

```
          ┌────────────────────────── config ───────────────────────────┐
          │   (wiring only: beans, security chain, caching switch)      │
          └──────────────────────────────────────────────────────────────┘
   web ───────────────▶ application ◀─────────────── infrastructure
 (controllers, DTOs,     (use cases + ports)          (PokéAPI adapter, JPA adapters,
  error mapping)               │                       security adapters)
                               ▼
                            domain
                    (entities, value objects, rules)
```

Design notes:

- [docs/architecture.md](docs/architecture.md) — layers, ports, flows, tests
- [docs/api-contract.md](docs/api-contract.md) — routes, payloads, errors
- [docs/data-model.md](docs/data-model.md) — tables and the seed
- [docs/pokeapi-integration.md](docs/pokeapi-integration.md) — upstream calls and caching
- [docs/requirements.md](docs/requirements.md) — scope and traceability

## Run with Docker

Docker is enough. From the repository root:

```bash
docker compose up --build
```

| URL | What |
|-----|------|
| http://localhost | Web UI |
| http://localhost:8080 | API (`/api/v1`) |
| http://localhost:8080/swagger-ui/index.html | Swagger UI |
| http://localhost:8080/actuator/health | Backend health |

Demo user (created on startup, not stored in SQL): **ash** / **Pikachu#2026**.

Flyway loads a snapshot of Pokémon 1–7, 133–136 and 10033. Bulbasaur, Charizard and Eevee already have extras. Log in, open one of those, edit the extras, and refresh: the change is in Postgres. Sync any other Pokémon from its detail page before editing it.

These values are for the demo only. Override them in the environment; do not reuse them anywhere real:

```bash
# PowerShell
$env:POKEHUB_JWT_SECRET = "a-different-secret-at-least-32-bytes"
$env:POSTGRES_PASSWORD = "a-different-db-password"
docker compose up --build
```

The database lives in the named volume `pgdata`. `docker compose down` keeps it. `docker compose down -v` drops it; the next start applies Flyway and creates the demo user again.

## Run tests

Backend, from `backend/` (JaCoCo runs as part of `check`):

```bash
# Windows
gradlew.bat check

# macOS / Linux
./gradlew check
```

HTML coverage report: `backend/build/reports/jacoco/test/html/index.html`. The gate is line coverage ≥ 90% for `domain` and `application`, and ≥ 80% overall. `PokehubApplication` and `config` are excluded from that gate (bootstrap and wiring).

Front end, from `frontend/`:

```bash
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

## Run locally without the app containers

Start only Postgres (same demo database defaults as Compose):

```bash
docker compose up postgres
```

API, from `backend/`. The JWT secret has no default in the application; startup fails if it is missing or shorter than 32 bytes.

```bash
# PowerShell
$env:POKEHUB_JWT_SECRET = "pokehub-demo-jwt-secret-not-for-production"
gradlew.bat bootRun
```

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` default to `localhost`, `5432`, `appdb`, `postgres` and `postgrespassword`.

Front end, from `frontend/`:

```bash
npm ci
npm run dev
```

Vite serves http://localhost:5173 and proxies `/api` to http://localhost:8080. Copy `frontend/.env.example` to `frontend/.env` only if you need a different `VITE_API_BASE_URL`.

## API

Base path `/api/v1`. Errors are RFC 9457 problem details. The contract, including validation and status codes, is [docs/api-contract.md](docs/api-contract.md). A generated snapshot is [docs/openapi.json](docs/openapi.json).

| Method and path | Access |
|-----------------|--------|
| `GET /pokemon` | public |
| `GET /pokemon/{id}` | public |
| `POST /pokemon/{id}/sync` | JWT |
| `DELETE /pokemon/{id}/sync` | JWT |
| `PUT /pokemon/{id}/extras` | JWT |
| `POST /auth/register`, `POST /auth/login` | public |

Send `Authorization: Bearer <accessToken>` from `POST /auth/login`.

## Interpretations

Short form of [docs/requirements.md](docs/requirements.md). These are the decisions taken where the exercise was ambiguous.

| # | Decision |
|---|----------|
| A1 | Skills are PokéAPI abilities, exposed as `skills: [{name, hidden}]`. |
| A2 | Category is the English genus. `null` when there is no English genus. |
| A3 | Mass is PokéAPI `weight` in hectograms, stored as an integer, exposed in kilograms. |
| A4 | The list always comes from PokéAPI (cached). The database is not a cache. |
| A5 | Sync copies every field the UI shows, plus extras. It is explicit and idempotent. Re-sync refreshes the snapshot and does not touch extras. |
| A6 | Editable fields are `localizedName`, `region` and `tags`. The snapshot is read-only. |
| A7 | Detail is the local replica when the Pokémon is synced, otherwise PokéAPI. |
| A8 | Pagination is `limit` (default 20, max 50) and `offset`, with `count`, `next`, `previous`, `results`. |
| A9 | Any upstream failure fails the whole request with 502. No partial pages. |
| A10 | Reads are public. Every write requires a JWT. |
| A11 | No audit columns or versions. Concurrent updates are last-write-wins. |
| A12 | Tests are unit tests only: no database, Testcontainers, H2 or network. |
| A13 | A replica is a static snapshot. Nothing refreshes it automatically. |

## Testing strategy

Behaviour was written test-first. Domain rules are plain JUnit tests. Use cases run against hand-written fakes. The PokéAPI adapter is tested with `MockRestServiceServer` and JSON fixtures. Cache behaviour uses a small Spring test context and an in-memory cache manager. Persistence adapters are tested with mocked Spring Data repositories; SQL is checked by running the app against Docker Postgres, not by a database test. Controllers use standalone `MockMvc`. Route security uses a `@WebMvcTest` slice. The front end tests behaviour through the DOM with Vitest and Testing Library.

Not used: `@SpringBootTest`, `@DataJpaTest`, Testcontainers, H2, or live HTTP in the test suite.

## Trade-offs and limitations

- One Gradle module, with ArchUnit instead of separate modules.
- The application layer has no Spring annotations, so wiring lives in `config`.
- One failing PokéAPI call fails the page.
- Sync is an explicit action, so a GET never writes, and an edit of an unsynced Pokémon is a 404.
- Re-sync never overwrites extras, and it also never corrects them from PokéAPI.
- Evolution sprites are built from PokéAPI's sprite URL shape, without an extra request per node.
- Last-write-wins updates can lose a concurrent edit.
- Replicated data does not expire. The API has no rate limit, no refresh tokens and no key rotation.
- The access token is kept in `sessionStorage`. It disappears when the tab closes, and any script on the page can still read it.

Demo passwords and the demo JWT secret are in this repository on purpose so `docker compose up --build` works with no extra files. They are not production secrets.

## How AI was used

Tools: Cursor, with an agent editing this repository. <!-- FILL: model name you actually used -->

Workflow: for each slice, the agent wrote a plan and waited. Implementation was test-first: a failing test, the minimum code that passed, then a refactor. I reviewed the diff before keeping it.

What I verified and corrected:

- <!-- EXAMPLE: a test or behaviour you rejected or rewrote, and why -->
- <!-- EXAMPLE: a Docker, API or schema mistake you caught in review -->
- <!-- EXAMPLE: something you checked by hand (a request, a screen, a coverage report) and what you changed afterwards -->
