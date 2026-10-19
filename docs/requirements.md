# Requirements and traceability

Source: the "Java - Technical Interview Exercise" (RESTful API with Java and Spring Boot, Clean Architecture, TDD,
PokéAPI integration, React front end, Docker). This file restates the scope, records every interpretation made where the
exercise is ambiguous, and maps each requirement to its implementation. It is the scope boundary of the project:
anything not listed here is out of scope.

## 1. User stories

| ID | Requirement (paraphrased) | Endpoint | Classes | Tests |
|----|---------------------------|----------|---------|-------|
| US01 | Browse Pokémon in paginated form; each entry shows sprite, category, mass and its skills. Nice to have: cache service responses. | `GET /api/v1/pokemon` | `PokemonController`, `ListPokemonService`, `PageQuery`, `PokeApiPokemonCatalog`, `JpaPokemonStore.findSyncedIds` | `ListPokemonServiceTest`, `PageQueryTest`, `PokemonControllerTest`, `PokeApiPokemonCatalogTest`, `PokeApiPokemonCatalogCacheTest`, `PokemonListPage.test.tsx`, `usePokemonList.test.tsx` |
| US02 | View one Pokémon in detail: image, core statistics, narrative description, evolutionary lineage. | `GET /api/v1/pokemon/{id}` | `PokemonController`, `GetPokemonDetailsService`, `PokeApiPokemonCatalog`, `EvolutionChainMapper`, `JpaPokemonStore` | `GetPokemonDetailsServiceTest`, `PokeApiPokemonCatalogDetailsTest`, `EvolutionChainMapperTest`, `PokemonDetailPage.test.tsx`, `usePokemonDetail.test.tsx`, `EvolutionTree.test.tsx` |
| US03 | Persist Pokémon data into a local relational store so proprietary fields can be added. | `POST /api/v1/pokemon/{id}/sync`, `DELETE /api/v1/pokemon/{id}/sync` | `SyncPokemonService`, `UnsyncPokemonService`, `JpaPokemonStore` (`saveSnapshot` keeps extras) | `SyncPokemonServiceTest`, `UnsyncPokemonServiceTest`, `JpaPokemonStoreTest`, `PokemonControllerTest`, `PokemonDetailPage.test.tsx` |
| US04 | Update any locally stored Pokémon. 404 when it is not stored, 400 for a malformed payload. | `PUT /api/v1/pokemon/{id}/extras` | `UpdatePokemonExtrasService`, `PokemonExtras`, `JpaPokemonStore.updateExtras` | `UpdatePokemonExtrasServiceTest`, `PokemonExtrasTest`, `PokemonControllerTest`, `EditExtrasPage.test.tsx`, `useUpdateExtras.test.tsx`, `validate-extras.test.ts` |

## 2. Technical requirements checklist

| Requirement | Where it is satisfied |
|-------------|-----------------------|
| Public Git repository, tests, proper error handling | Repository tests; `GlobalExceptionHandler` returns RFC 9457 `ProblemDetail` (`PokemonControllerTest`, `AuthControllerTest`) |
| Caching layer for PokéAPI responses (nice to have) | Caffeine via `CacheConfig` and `@Cacheable` on `PokeApiPokemonCatalog`; `PokeApiPokemonCatalogCacheTest` |
| Front end consuming the API (React) | `frontend/src` (`AppRouter`, feature pages, `frontend/src/api/client.ts`) |
| Relational database with a primary entity and a secondary collection for user management; unique PK and at least two descriptive attributes | `V1__create_schema.sql`: `pokemon` (and `species`, `ability`, stats, tags) and `users` (`id`, `username`, `password_hash`). See `docs/data-model.md` |
| Web API with CRUD, standard HTTP verbs, required parameters, consistent return structures | `PokemonController`, `AuthController`; contract in `docs/api-contract.md`; `OpenApiContractTest` |
| Auxiliary API for registration, authentication, protected versus public routes | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`; `SecurityConfig`; `RegisterUserService`, `AuthenticateUserService`; `RouteSecurityTest` |
| Specialized data access layer | `JpaPokemonStore`, `JpaUserStore` behind `PokemonStore` and `UserStore`; `JpaPokemonStoreTest`, `JpaUserStoreTest` |
| Core business logic layer independent from API and data access | `domain`, `application` (no framework imports); `ArchitectureTest` |
| Thorough unit tests for every core component | `backend/src/test/java`, `frontend/src/**/*.test.*`; strategy in `docs/architecture.md` §9. Gate: `gradlew check` |
| Responsive, user-centric UI; CRUD on use cases; clean component organization; efficient state management; no browser console warnings | `frontend/src/features` (list, detail, sync/edit, auth), TanStack Query, `ProtectedRoute.test.tsx` |
| README, seeded demo data or mock credentials, Dockerfile | `README.md`; `V2__seed_demo_data.sql` (Pokémon snapshot); `DemoUserSeeder` (`ash` / `Pikachu#2026`, `DemoUserSeederTest`); `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` |
| Generative AI exercise | Presented in the interview; how AI was used in this project is summarized in the README |

## 3. Interpretations and assumptions

| # | Decision |
|---|----------|
| A1 | "Skills" are PokéAPI **abilities** (not moves). Exposed in the API as `skills: [{name, hidden}]`. |
| A2 | "Category" is the English **genus** of the species (e.g. "Seed Pokémon"); `null` when no English genus exists. |
| A3 | "Mass" is PokéAPI `weight` (hectograms). Stored as an integer in hectograms, exposed as `mass` in kilograms. |
| A4 | US01 listing always comes from PokéAPI (cached). The database is **not** a cache. |
| A5 | US03 replicates **all data the system displays** (US01 + US02 fields) plus proprietary extras. Sync is an explicit, idempotent action; re-sync refreshes the snapshot and never touches the extras. |
| A6 | Extras are `localizedName`, `region` and `tags`. They are the only user-editable fields (US04). The PokéAPI snapshot is read-only. |
| A7 | Detail (US02) is served from the local replica when the Pokémon is synced, otherwise from PokéAPI. |
| A8 | Pagination mirrors PokéAPI: `limit` (default 20, max 50) and `offset`; response has `count`, `next`, `previous`, `results`. |
| A9 | Any upstream failure fails the whole request (fail fast) with 502; no partial pages. |
| A10 | Reads are public; every write requires a JWT. |
| A11 | No audit columns or optimistic-lock versions for now: concurrent updates are last-write-wins. |
| A12 | Only unit tests: no database, Testcontainers, H2 or network in tests. |
| A13 | Replicated data is a static snapshot; no automatic refresh or expiry. |

## 4. Out of scope

GraphQL, roles/permissions beyond "authenticated", refresh tokens, rate limiting, bulk sync, automatic refresh of
replicated data, internationalization of the UI, i18n of API messages.
