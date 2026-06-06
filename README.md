# Centre Profile Information

A full-stack web application for managing licensed childcare centres in Singapore. Built for ECDA (Early Childhood Development Agency) officers, HQ administrators, and centre leaders.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript + Vite |
| Backend | Kotlin + Spring Boot 3 |
| Database | PostgreSQL + Flyway migrations |
| Auth | JWT (stateless, role-based) |
| E2E Tests | Playwright |

## Features

- **Centre list** — searchable, filterable, sortable directory of licensed centres
- **Centre profile** — inline editing of profile fields (officers and admins)
- **KAH management** — Key Appointment Holder history with rotation tracking
- **Contact management** — centre contact records by type
- **Lifecycle events** — append-only audit log of status changes and updates
- **Waiver history** — read-only view of regulatory waivers (ECDA Officers only)

## Roles & Access

| Role | Centre List | Profile (read) | Profile (edit) | Waiver History |
|------|:-----------:|:--------------:|:--------------:|:--------------:|
| `ECDA_OFFICER` | ✓ | ✓ | ✓ | ✓ |
| `HQ_ADMIN` | ✓ | ✓ | ✓ | — |
| `CENTRE_LEADER` | ✓ | ✓ | — | — |

## Prerequisites

- Node.js 18+
- JDK 17+
- PostgreSQL (local instance, `trust` auth in `pg_hba.conf`)

## Getting Started

### 1. Start the backend

```bash
cd backend
./gradlew bootRun
```

The API starts on `http://localhost:8080`. Flyway runs migrations automatically — no manual DB setup needed.

### 2. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI starts on `http://localhost:5173`.

### 3. Log in

Three test accounts are seeded (all share password `password`):

| Username | Role |
|----------|------|
| `officer1` | `ECDA_OFFICER` |
| `admin` | `HQ_ADMIN` |
| `leader1` | `CENTRE_LEADER` |

## Project Structure

```
.
├── backend/                  # Spring Boot API
│   └── src/main/kotlin/com/ecda/platform/
│       ├── config/           # Security, CORS, JWT
│       ├── controller/       # REST endpoints
│       ├── service/          # Business logic (CentreService)
│       ├── repository/       # Spring Data JPA repositories
│       ├── model/            # JPA entities
│       └── dto/              # Request/response shapes
│   └── src/main/resources/db/migration/   # Flyway migrations
├── frontend/                 # React SPA
│   └── src/
│       ├── pages/            # Route-level components
│       ├── components/       # UI components
│       ├── hooks/            # Async state (loading/error/data)
│       ├── services/         # Axios API client
│       ├── contexts/         # AuthContext (role + permissions)
│       └── types/            # TypeScript types
├── tests/                    # Playwright E2E / access-control tests
└── openspec/                 # Planning specs and change history
```

## Commands

### Frontend

```bash
cd frontend
npm run dev          # dev server on http://localhost:5173
npm run build        # type-check + production build
npm test             # unit tests (Vitest)
npm run lint         # ESLint
```

### Backend

```bash
cd backend
./gradlew bootRun    # start API (requires PostgreSQL)
./gradlew test       # all tests (H2 in-memory, no DB needed)
./gradlew build      # compile + test + jar

# Run a specific test class
./gradlew test --tests "*.CentreServiceTest"

# Run a specific test method
./gradlew test --tests "*.CentreServiceTest.createCentre*"
```

### E2E Tests

```bash
# Requires both backend and frontend running
npm run test:e2e
npm run test:e2e:report   # open HTML report
```

## Architecture Notes

**Auth flow**: JWT is issued at `POST /api/auth/login`, stored in `localStorage` under `ecda_token`, and sent as `Authorization: Bearer <token>` on every request via an Axios interceptor. `JwtAuthFilter` validates the token before each request reaches a controller.

**Backend layers**: `controller → service → repository`. All business logic and repository access is in `CentreService` — controllers never call repositories directly.

**KAH rotation**: When a new KAH is added, the current one is automatically marked `isCurrent=false` with an end date. History is preserved.

**Lifecycle events**: `centre_lifecycle_events` is append-only. Every state-changing operation records an event; rows are never updated or deleted.

**Flyway migrations**: Never edit existing migration files. Always add a new versioned file (`V{n}__description.sql`).

**H2 for tests**: The `test` profile (`@ActiveProfiles("test")`) switches to H2 in-memory. No running PostgreSQL is needed for any test.
