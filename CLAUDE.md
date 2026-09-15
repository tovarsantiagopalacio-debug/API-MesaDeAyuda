# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Helpdesk API ("Mesa de Ayuda"): a Spring Boot 3 / Java 17 REST API for ticket management with SLA tracking and JWT authentication. Domain and code (entities, DTOs, messages) are in Spanish. Uses an in-memory H2 database — all data resets on every restart.

## Build, run, test

There is no Maven wrapper in the repo (the `DockerFile` references `./mvnw` but it isn't committed) — use a locally installed `mvn`.

```bash
mvn clean package -DskipTests   # build the jar
mvn spring-boot:run             # run for development
java -jar target/api-1.0.0.jar  # run the built jar
```

The app starts on `http://localhost:8080`. There are no automated tests in this repo (no `src/test` directory) — verify changes by running the app and exercising endpoints (curl, the included `postman_collection.json`, or the H2 console).

H2 console: `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:helpdesk`, user `sa`, empty password.

`DataInitializer` (`src/main/java/com/helpdesk/api/config/DataInitializer.java`) seeds one ADMIN, one SOPORTE, and one USUARIO account plus sample tickets (including one pre-expired ticket) on every startup, only if the `usuarios` table is empty. Seeded credentials (also in README.md):

| Rol | Email | Password |
|-----|-------|----------|
| ADMIN | admin@helpdesk.com | admin123 |
| SOPORTE | soporte@helpdesk.com | soporte123 |
| USUARIO | usuario@helpdesk.com | usuario123 |

## Architecture

Standard layered Spring Boot structure under `src/main/java/com/helpdesk/api/`:

- `controller/` — REST endpoints, request validation (`@Valid`), delegates to `service/`. Authorization for role-gated endpoints is declared with `@PreAuthorize` (method security is enabled via `@EnableMethodSecurity` in `SecurityConfig`).
- `service/` — business logic (`AuthService`, `TicketService`). Controllers pass the authenticated principal's email/role down explicitly rather than services reading `SecurityContextHolder` themselves.
- `entity/` — JPA entities (`Usuario`, `Ticket`, `RefreshToken`) plus enums (`Rol`, `Prioridad`, `EstadoTicket`).
- `repository/` — Spring Data JPA repositories.
- `dto/` — request/response payloads, decoupled from entities.
- `security/` — `JwtTokenProvider` (signs/parses JWTs with `jjwt`) and `JwtAuthenticationFilter` (a `OncePerRequestFilter` that reads the `Authorization: Bearer` header, validates the token, and populates `SecurityContextHolder` with a `UsernamePasswordAuthenticationToken` whose authorities are `ROLE_<rol>`).
- `config/SecurityConfig` — stateless session, CSRF disabled, permits `/api/ping`, `/h2-console/**`, and POST to `/api/auth/{registro,login,refresh}`; everything else requires authentication.
- `config/DataInitializer` — dev-only seed data, see above.
- `exception/GlobalExceptionHandler` — `@RestControllerAdvice` mapping exception types to HTTP status: validation errors → 400, `IllegalArgumentException` → 409, `BadCredentialsException` → 401, `AccessDeniedException` → 403, any other `RuntimeException` → 404. When throwing exceptions from a service, pick the exception type deliberately — it directly determines the HTTP status returned.

### Auth flow

Two-token scheme: short-lived JWT access token (15 min, contains `email` subject + `rol` claim, not persisted) and a long-lived opaque refresh token (7 days, a random UUID persisted in the `refresh_tokens` table via `RefreshTokenRepository`). Refresh tokens are single-use and rotate: `AuthService.refresh()` revokes the presented token and issues a new access+refresh pair. Logout (`AuthService.logout()`) revokes **all** refresh tokens for the user, not just one session's. There is no in-memory Spring Security `UserDetailsService`/`AuthenticationManager` — login and token validation are handled directly in `AuthService`/`JwtTokenProvider` against `UsuarioRepository`.

### SLA business rule

`Ticket.onCreate()` (a `@PrePersist` hook) computes `slaVenceEn` from `prioridad` when not already set: ALTA → +4h, MEDIA → +24h, BAJA → +72h, from `creadoEn`. `Ticket.isVencido()` is a derived, non-persisted check (`estado != RESUELTO && now().isAfter(slaVenceEn)`) — don't add a persisted "vencido" column, keep it computed. `TicketRepository.findVencidos()` is the query-level equivalent used for the `/api/tickets/vencidos` listing.

### Roles / authorization model

Three roles: `USUARIO` (default on self-registration via `/api/auth/registro`), `SOPORTE`, `ADMIN`. Only an existing ADMIN can promote a user to SOPORTE (`POST /api/admin/soporte`, `@PreAuthorize("hasRole('ADMIN')")`) — there is no path to create an ADMIN via the API; that role only exists via `DataInitializer`. Ticket visibility: a ticket's owner can always see their own ticket; SOPORTE/ADMIN can see and act on any ticket. This ownership check lives in `TicketService.obtener()`, not in `@PreAuthorize`, because it depends on comparing the ticket's `creadoPor` to the caller.

See `README.md` for the full endpoint table and `postman_collection.json` for example requests.
