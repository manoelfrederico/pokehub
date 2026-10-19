# Architecture

Pokehub is a Spring Boot REST API (plus a React front end) that browses Pokémon from PokéAPI, replicates chosen Pokémon
into PostgreSQL and lets authenticated users enrich them with proprietary fields. It follows Clean Architecture
(ports and adapters), is built test-first, and is covered by unit tests only.

## 1. Drivers

1. Business rules independent from frameworks, HTTP and the database (the exercise grades architectural independence).
2. Every behaviour specified by a failing unit test first (TDD) and every scenario covered, including failures.
3. Safe use of an external API we do not control: cached, bounded concurrency, translated into our own model.
4. Small, readable code (Clean Code), modern Java 25 and Spring Boot 4.

## 2. Stack

| Concern | Choice |
|---------|--------|
| Language / build | Java 25 (LTS), Gradle (wrapper). No preview features. |
| Framework | Spring Boot 4.1.x (newest stable at project creation), Spring Framework 7, Jackson 3 |
| External API client | `RestClient` over the JDK `HttpClient`, built from Spring Framework API (no Boot auto-config dependency) |
| Cache | Spring Cache + Caffeine (`spring.cache.caffeine.spec`) |
| Persistence | PostgreSQL, Spring Data JPA (Hibernate), Flyway (`ddl-auto=validate`) |
| Security | Spring Security, OAuth2 Resource Server (JWT, HS256), BCrypt |
| API docs | springdoc-openapi (the release line that targets Spring Boot 4) |
| Tests | JUnit 5, AssertJ, Mockito (sparingly), `MockRestServiceServer`, MockMvc, ArchUnit, JaCoCo |
| Front end | React + Vite + TypeScript, Tailwind CSS, TanStack Query, React Router, Vitest + Testing Library |

Dependencies are added only when the phase that needs them is implemented. Never hand-pick versions managed by the
Boot BOM. In Boot 4 auto-configuration is modular: if a library is on the classpath but not configured, look for the
matching `spring-boot-starter-*` instead of writing manual configuration.

## 3. Layers and the dependency rule

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

Allowed dependencies (everything else is forbidden and enforced by `ArchitectureTest`):

| Layer | May depend on | Must never depend on |
|-------|---------------|----------------------|
| `domain` | JDK only | Spring, Jackson, JPA, Jakarta, other layers |
| `application` | `domain`, JDK | Spring, Jackson, JPA, `web`, `infrastructure` |
| `infrastructure` | `application` (ports), `domain`, frameworks | `web` |
| `web` | `application` (use cases), `domain`, Spring Web | `infrastructure` |
| `config` | all layers | — |

Rules of thumb:
- Business rules and validation live in `domain` (invariants) and `application` (orchestration). Controllers translate, adapters translate; neither decides.
- Framework annotations never appear in `domain` or `application`. Use cases are plain classes wired in `config`.
- Each adapter owns its model (PokéAPI DTOs, JPA entities, HTTP DTOs). Mapping happens at the boundary; the domain never sees them.
- Transactions are opened by the persistence adapter. Calls to PokéAPI are never made inside a transaction.

## 4. Package structure

Root package `com.fred.pokehub` (adjust to the Gradle group; keep it consistent).

```
com.fred.pokehub
├─ domain
│  ├─ model        Mass, Ability, Stat, EvolutionNode, PokemonSummary, PokemonDetails, PokemonExtras,
│  │               StoredPokemon, PokemonView, PageQuery, PokemonPage, PokemonReference, PokemonReferencePage, User
│  └─ exception    InvalidPaginationException, InvalidPokemonExtrasException, InvalidUserException
├─ application
│  ├─ port.in      ListPokemonUseCase, GetPokemonDetailsUseCase, SyncPokemonUseCase, UnsyncPokemonUseCase,
│  │               UpdatePokemonExtrasUseCase, RegisterUserUseCase, AuthenticateUserUseCase
│  ├─ port.out     PokemonCatalog, PokemonStore, UserStore, PasswordHasher, AccessTokenIssuer
│  ├─ service      one class per use case (ListPokemonService, ...)
│  └─ exception    CatalogUnavailableException, PokemonNotFoundException, PokemonNotSyncedException,
│                  UsernameAlreadyTakenException, InvalidCredentialsException
├─ infrastructure
│  ├─ pokeapi      PokeApiPokemonCatalog (+ DTOs, mappers, sprite policy, URL helpers, properties, client config)
│  ├─ persistence  JPA entities, Spring Data repositories, mappers, JpaPokemonStore, JpaUserStore
│  └─ security     BcryptPasswordHasher, JwtAccessTokenIssuer, SecurityConfig, JWT properties
├─ web             controllers, request/response records, GlobalExceptionHandler (one sub-package per resource)
└─ config          UseCaseConfig (use case beans), CacheConfig, DemoUserSeeder wiring
```

## 5. Domain model

- `Mass(hectograms)` → `kilograms()`; `Ability(id, name, hidden)`; `Stat(name, baseValue)`. `id` is the PokéAPI ability id.
- `PokemonSummary(id, name, spriteUrl, category, mass, abilities, synced)` — listing read model (US01).
- `PokemonDetails(id, name, imageUrl, spriteUrl, category, mass, abilities, stats, description, evolutionChain, speciesId, speciesName)` — the PokéAPI snapshot (US02/US03). `speciesId` is the species resource id.
- `EvolutionNode(id, name, spriteUrl, evolvesTo)` — recursive and immutable.
- `PokemonExtras(localizedName, region, tags)` — proprietary fields, validated and normalized in the constructor (US03/US04).
- `StoredPokemon(details, extras)` — what the local replica holds. `PokemonView(details, extras, synced)` — what the detail endpoint returns.
- `PageQuery(limit, offset)`, `PokemonPage(count, query, items)` with `nextOffset()` / `previousOffset()` — the pagination rules live in the domain.
- `User(id, username, passwordHash)`.

All models are immutable records with invariants checked in the compact constructor; collections are defensively copied.

## 6. Ports

| Port | Direction | Responsibility |
|------|-----------|----------------|
| `ListPokemonUseCase`, `GetPokemonDetailsUseCase`, `SyncPokemonUseCase`, `UnsyncPokemonUseCase`, `UpdatePokemonExtrasUseCase`, `RegisterUserUseCase`, `AuthenticateUserUseCase` | in | One method each; implemented by the services in `application.service` |
| `PokemonCatalog` | out | Read-only gateway to PokéAPI: `listReferences`, `findSummaryById`, `findDetailsById` |
| `PokemonStore` | out | Local replica: `findById`, `findSyncedIds(ids)`, `saveSnapshot(details)` (upsert that preserves extras), `updateExtras`, `deleteById` |
| `UserStore` | out | `findByUsername`, `existsByUsername`, `save` |
| `PasswordHasher` | out | `hash`, `matches` |
| `AccessTokenIssuer` | out | `issue(User)` → token + expiry |

## 7. Key flows

**US01 — list.** `ListPokemonService` asks `PokemonCatalog.listReferences(PageQuery)`, then loads each summary concurrently
(virtual threads) through `findSummaryById`, preserving order, failing fast and cancelling in-flight work on the first error.
Caching sits on the adapter methods (so the service's calls go through the Spring proxy). When US03 lands, the service also
asks `PokemonStore.findSyncedIds(ids)` once and flags synced entries.

**US02 — detail.** `GetPokemonDetailsService` checks `PokemonStore.findById`; if present returns it (details + extras,
`synced = true`), otherwise `PokemonCatalog.findDetailsById` (pokemon → species → evolution chain, sequential because each step needs the previous one).

**US03 — sync.** `SyncPokemonService` loads `findDetailsById` (outside any transaction), then `PokemonStore.saveSnapshot`
(single transaction, upsert; extras untouched). Returns whether it was created or refreshed (HTTP 201 / 200).
`UnsyncPokemonService` deletes the local copy (204, or 404 if not synced).

**US04 — update extras.** `UpdatePokemonExtrasService` requires the Pokémon to be stored (`PokemonNotSyncedException` → 404), builds a
validated `PokemonExtras` (domain rules → 400) and saves it.

**Auth.** `RegisterUserService` (unique username, password policy, hash) and `AuthenticateUserService` (verify, issue JWT).
Spring Security validates the JWT on every protected route; the use cases never see tokens.

## 8. Cross-cutting concerns

- **Errors.** One `@RestControllerAdvice` extending `ResponseEntityExceptionHandler`, always returning RFC 9457 `ProblemDetail`. Mapping in `docs/api-contract.md`. 5xx responses never leak internals; causes are logged once.
- **Caching.** Caches `pokemon-references` (key: `PageQuery`), `pokemon-summaries`, `pokemon-details`, `evolution-chains`; Caffeine, max size and TTL from `application.yml`. Failures are never cached.
- **Concurrency toward PokéAPI.** Virtual-thread executor in the use case; a `Semaphore` in the adapter caps in-flight HTTP calls (`pokehub.pokeapi.max-concurrent-requests`).
- **Configuration.** `@ConfigurationProperties` records validated at startup; secrets only via environment variables.
- **Logging.** SLF4J; no sensitive data (passwords, tokens) in logs.

## 9. Testing strategy (unit tests only)

| Subject | Technique |
|---------|-----------|
| Domain records and rules | Plain JUnit + AssertJ, parameterized for boundaries |
| Use cases | Hand-written fakes of the out ports (no Mockito for ports); deterministic, including concurrency and cancellation |
| PokéAPI adapter | `MockRestServiceServer` bound to a `RestClient.Builder`; real JSON fixtures; failure matrix |
| Cache behaviour | Minimal `@SpringJUnitConfig` with `@EnableCaching` + `ConcurrentMapCacheManager` (no Boot, no network, no DB) |
| Persistence adapter | Mappers tested directly; adapter tested with Mockito mocks of the Spring Data interfaces |
| Controllers | Standalone `MockMvc` with the real `GlobalExceptionHandler` |
| Security rules | `@WebMvcTest` slice with mocked use cases (no database) |
| Architecture | ArchUnit rules mirroring §3 |
| Front end | Vitest + Testing Library |

Forbidden: `@SpringBootTest`, `@DataJpaTest`, Testcontainers, H2, real network calls, sleeping to wait for async work.
Quality gate: JaCoCo line coverage ≥ 90 % for `domain` and `application`, ≥ 80 % overall, enforced in `gradlew check`.

## 10. Decisions and trade-offs

| Decision | Why | Trade-off |
|----------|-----|-----------|
| Single Gradle module with enforced package rules | Fast to build and review | Weaker than multi-module isolation; mitigated by ArchUnit |
| Application layer has no Spring annotations | Real framework independence | One extra `config` class for wiring |
| Service orchestrates per-item loading, adapter caches per item | Spring cache proxies are bypassed on self-invocation | Two port methods instead of one |
| Fail-fast on any upstream error | Simple, honest, testable | One failing item breaks the page |
| REST instead of PokéAPI's GraphQL beta | Stable, documented, what the exercise links to | More calls per page, absorbed by cache + concurrency |
| Replica via explicit sync, not read-through | GET stays side-effect free; US04's 404 stays meaningful | Users must sync before editing |
| Snapshot read-only, extras editable | Replicated data stays trustworthy; re-sync never loses user data | Cannot correct PokéAPI data locally |
| Evolution node sprite by URL convention | No extra call per node | Assumes PokéAPI's sprite path stays stable |
| Last-write-wins updates | Scope control (no version column yet) | Lost updates possible under concurrency |

## 11. Known limitations

Replicated data never refreshes automatically; no rate limiting on our own API; one JWT secret, no key rotation; no refresh tokens.
