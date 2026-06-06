# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Centre Profile Information — A full-stack app for managing licensed childcare centres in Singapore.

- **Frontend**: React 18 + TypeScript + Vite (`frontend/`)
- **Backend**: Kotlin + Spring Boot 3 (`backend/`)
- **Database**: PostgreSQL with Flyway migrations

## Commands

### Frontend
```bash
cd frontend
npm install          # install deps
npm run dev          # dev server on http://localhost:5173
npm run build        # type-check + build
npm test             # run vitest
npm run test:coverage
npm run lint
```

### Backend
```bash
cd backend
./gradlew bootRun                          # run Spring Boot (requires PostgreSQL)
./gradlew test                             # all tests (uses H2 in-memory, no DB needed)
./gradlew test --tests "*.CentreServiceTest"   # single test class
./gradlew test --tests "*.CentreServiceTest.createCentre*"  # single test method
./gradlew build                            # compile + test + jar
```

### Database

Requires a local PostgreSQL instance with `pg_hba.conf` set to `trust`.
Connects to the default `postgres` database as the `postgres` user — no password needed.
Flyway runs migrations automatically on `bootRun` — no manual schema setup needed.

## Architecture

### Auth Flow
JWT is issued at `POST /api/auth/login`. The frontend stores it in `localStorage` under `ecda_token` and attaches it as `Authorization: Bearer <token>` via an Axios interceptor (`frontend/src/services/api.ts`). On the backend, `JwtAuthFilter` validates the token and populates `SecurityContextHolder` before each request hits a controller.

Three roles: `ECDA_OFFICER`, `HQ_ADMIN` (both can write), `CENTRE_LEADER` (read-only). Role is embedded in the JWT claim `role` and enforced by Spring Security's `@EnableMethodSecurity` + URL-level rules in `SecurityConfig`.

### Backend Layer Structure
```
controller → service → repository (Spring Data JPA)
```
- **Models** (`model/`): JPA entities. `Centre` owns `KahDetail`, `CentreContact`, `CentreLifecycleEvent` via `@OneToMany`. Enums (`CentreType`, `LicenceStatus`, `ContactType`, `UserRole`) are stored as strings in the DB.
- **DTOs** (`dto/CentreDto.kt`): All API shapes. `CentreProfileDto` is the full view; `CentreSummaryDto` is the list view. Request types have Bean Validation annotations.
- **CentreService**: The only place that touches repositories. Every state-changing operation records a `CentreLifecycleEvent`. KAH rotation logic: when a new KAH is added, the current one is marked `isCurrent=false` with an end date.
- **Tests use H2** (`application-test.yml`) with `@ActiveProfiles("test")` — no running Postgres needed. MockK is used for unit tests; `@WebMvcTest` + `SpringMockK` for controller slice tests.

### Frontend Layer Structure
```
pages/ → hooks/ → services/ → axios (api.ts) → backend
         ↓
     components/
```
- **`services/api.ts`**: Single Axios instance with auth interceptor and 401→redirect handling.
- **Hooks** (`useCentre`, `useCentreSearch`): All async state (loading/error/data) lives here, not in components.
- **`InlineEdit`** (`components/common/InlineEdit.tsx`): Click-to-edit pattern used throughout the profile page. Accepts a `validate` function for field-level validation. Only calls `onSave` if the value changed and validation passes.
- **`AuthContext`**: Wraps the app; `canEdit` is derived from role. `ProtectedRoute` in `App.tsx` redirects unauthenticated users to `/login`.

### Database Schema Key Points
- `centre_id` is a human-readable code (e.g. `CC-001`), separate from the auto-increment PK.
- `kah_details` supports history: multiple rows per centre, `is_current` flags the active one.
- `centre_lifecycle_events` is append-only — never updated, always inserted.
- Flyway seed data creates three test users (password hash = `password` via BCrypt).

### Search/Filter
`CentreRepository.search()` uses a JPQL query with nullable parameters — null parameters are treated as "no filter". Sorting is applied via `Sort.by(direction, field)` before passing a `Pageable` to the query.

## Key Files
- `frontend/src/services/api.ts` — Axios instance; touch this for auth changes
- `frontend/src/context/AuthContext.tsx` — role-based `canEdit` flag
- `frontend/src/components/common/InlineEdit.tsx` — reusable click-to-edit pattern
- `backend/.../service/CentreService.kt` — all business logic lives here
- `backend/.../security/SecurityConfig.kt` — role-based access rules
- `backend/.../security/JwtAuthFilter.kt` — token validation per request
- `backend/src/main/resources/db/migration/` — Flyway migrations (never edit existing ones)

## Code Conventions

### Kotlin / Backend
- Use `data class` for DTOs, avoid mutable state
- All repository access goes through `CentreService` — never call repositories from controllers
- Every state-changing operation must record a `CentreLifecycleEvent`
- Enums stored as strings in DB — never as ordinals
- Bean Validation annotations on all request DTOs (`@NotBlank`, `@Size`, etc.)

### React / Frontend
- Functional components only — no class components
- All async state (loading / error / data) lives in hooks, never in components
- Use `canEdit` from `AuthContext` to gate any write action in the UI
- API errors go to user-facing toast/message — not `console.log`
- `InlineEdit` is the standard pattern for editable fields on the profile page

### General
- Frontend validation must mirror backend Bean Validation — keep them in sync
- Never write H2-specific SQL in Flyway migrations (H2 is test-only)
- `centre_id` (e.g. `CC-001`) is the human-readable identifier; the auto-increment PK is internal only

## Common Pitfalls
- **Never** call repositories directly from controllers — always go through `CentreService`
- **KAH updates** must use the rotation logic in `CentreService` (marks old KAH `isCurrent=false`) — never do direct saves
- **Flyway migrations** are immutable once committed — never edit existing migration files, always add new ones
- **H2 is test-only** — `@ActiveProfiles("test")` switches to H2; production always uses PostgreSQL
- **JWT role claim** is `role` (singular) — don't confuse with Spring Security's `ROLE_` prefix convention
- **`centre_lifecycle_events`** is append-only — never update or delete rows in this table

## Testing
- Unit tests: MockK for mocking, always use `@ActiveProfiles("test")`
- Controller tests: `@WebMvcTest` + `SpringMockK`
- No real PostgreSQL needed for any test — H2 in-memory only
- Run a single test class: `./gradlew test --tests "*.ClassName"`
- Run a single method: `./gradlew test --tests "*.ClassName.methodName*"`

## Available Skills

### Project-Specific
- `frontend-design` — building React components, pages, or UI elements with high design quality
- `vercel-react-best-practices` — React/Next.js performance optimization and best practices
- `web-design-guidelines` — review UI code for Web Interface Guidelines compliance and accessibility

### Code Quality & Review
- `code-review` — review PRs and current changes for correctness, bugs, and simplifications
- `security-review` — security review of pending changes on the current branch
- `simplify` — refactor code for reuse, simplification, and efficiency
- `verify` — verify changes work in the running app before committing

### Development Workflow
- `run` — launch and drive the app to see changes working
- `loop` — run tasks on recurring intervals (e.g., `loop 5m /foo`)
- `schedule` — create scheduled remote agents that run on a cron schedule
- `claude-api` — build, debug, and optimize Claude API / Anthropic SDK apps

### Setup & Configuration
- `claude-code-setup` — analyze codebase and recommend Claude Code automations
- `update-config` — configure Claude Code via settings.json (permissions, env vars, hooks)
- `keybindings-help` — customize keyboard shortcuts in ~/.claude/keybindings.json
- `fewer-permission-prompts` — reduce permission prompts by allowlisting common tools

### General Utilities
- `find-skills` — discover and install new agent skills
- `init` — initialize a new CLAUDE.md file with codebase documentation
- `review` — review a pull request
- `html-builder` — HTML builder for creating web content

# LicenseHub Project Instructions
- Always apply these skills:
- test-gate
- Before every commit:
- Run all relevant tests
- Verify build success
- Fix failures first
- Never commit failing code

