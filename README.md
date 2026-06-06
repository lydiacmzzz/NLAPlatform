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

- **Centre list** — searchable, filterable, sortable directory of licensed centres; scoped per user role
- **Centre profile** — inline editing of profile fields (officers and HQ admins)
- **KAH management** — Key Appointment Holder history with rotation tracking
- **Contact management** — centre contact records by type
- **Lifecycle events** — append-only audit log of status changes and updates
- **Waiver history** — read-only view of regulatory waivers (ECDA Officers only, scoped to their HQs)
- **HQ-based row-level access control** — server-enforced visibility scoping per role

## Roles & Access

| Role | Visibility Scope | Centre List | Profile (read) | Profile (edit) | Waiver History |
|------|-----------------|:-----------:|:--------------:|:--------------:|:--------------:|
| `ECDA_OFFICER` | Assigned HQs | ✓ | ✓ | ✓ | ✓ |
| `HQ_ADMIN` | Own HQ only | ✓ | ✓ | ✓ | — |
| `CENTRE_LEADER` | Own centre only | ✓ | ✓ | — | — |

Access control is enforced server-side for every endpoint — roles alone are not sufficient; the requested centre must also fall within the user's assigned scope.

## Prerequisites

- Node.js 18+
- JDK 17+
- PostgreSQL (local instance, `trust` auth in `pg_hba.conf`)

## Getting Started

### 1. Start the backend

```bash
cd backend
./gradlew bootRun
The API starts on http://localhost:8080. Flyway runs migrations automatically — no manual DB setup needed.

2. Start the frontend

cd frontend
npm install
npm run dev
The UI starts on http://localhost:5173.

3. Log in
Three test accounts are seeded (all share password password):

Username	Role	Scope
officer1	ECDA_OFFICER	HQ-A + HQ-B (CC-001 to CC-004)
HQAAdmin	HQ_ADMIN	HQ-A only (CC-001, CC-002)
HQACenterLeader1	CENTRE_LEADER	CC-001 only
Project Structure

.
├── backend/                  # Spring Boot API
│   └── src/main/kotlin/com/ecda/platform/
│       ├── config/           # Security, CORS, JWT
│       ├── controller/       # REST endpoints
│       ├── service/          # Business logic (CentreService, CentreScopeService)
│       ├── repository/       # Spring Data JPA repositories
│       ├── model/            # JPA entities (Centre, Hq, User, OfficerHqAssignment, …)
│       └── dto/              # Request/response shapes
│   └── src/main/resources/db/migration/   # Flyway migrations (immutable)
├── frontend/                 # React SPA
│   └── src/
│       ├── pages/            # Route-level components
│       ├── components/       # UI components
│       ├── hooks/            # Async state (loading/error/data)
│       ├── services/         # Axios API client
│       ├── contexts/         # AuthContext (role + canEdit + canViewWaivers)
│       └── types/            # TypeScript types
├── tests/                    # Playwright E2E / access-control tests
│   └── access-control/
│       ├── centre-visibility.spec.ts   # HQ-scoping API tests (all three roles)
│       └── waiver-history.spec.ts      # Waiver access-control tests
└── openspec/                 # Planning specs and archived change history
    ├── specs/                # Canonical capability specs
    └── changes/archive/      # Completed change records
Commands
Frontend

cd frontend
npm run dev          # dev server on http://localhost:5173
npm run build        # type-check + production build
npm test             # unit tests (Vitest)
npm run lint         # ESLint
Backend

cd backend
./gradlew bootRun    # start API (requires PostgreSQL)
./gradlew test       # all tests (H2 in-memory, no DB needed)
./gradlew build      # compile + test + jar

# Run a specific test class
./gradlew test --tests "*.CentreServiceTest"

# Run a specific test method
./gradlew test --tests "*.CentreServiceTest.createCentre*"
E2E Tests

# Requires both backend and frontend running
npm run test:e2e
npm run test:e2e:report   # open HTML report
Architecture Notes
Auth flow: JWT is issued at POST /api/auth/login, stored in localStorage under ecda_token, and sent as Authorization: Bearer <token> on every request via an Axios interceptor. JwtAuthFilter validates the token before each request reaches a controller.

Backend layers: controller → service → repository. All business logic and repository access is in CentreService — controllers never call repositories directly.

HQ-based scoping: CentreScopeService resolves the calling user's CentreScope from the JWT on every request. It builds a JPA Specification<Centre> that ANDs into every list query, and calls assertInScope before returning any individual centre. Scope variants are sealed Kotlin classes: OfficerScope(hqIds), AdminScope(hqId), LeaderScope(centreId).

KAH rotation: When a new KAH is added, the current one is automatically marked isCurrent=false with an end date. History is preserved.

Lifecycle events: centre_lifecycle_events is append-only. Every state-changing operation records an event; rows are never updated or deleted.

Flyway migrations: Never edit existing migration files. Always add a new versioned file (V{n}__description.sql).

H2 for tests: The test profile (@ActiveProfiles("test")) switches to H2 in-memory. No running PostgreSQL is needed for any test.

Database Schema Overview
Table	Purpose
centres	Core centre records; centre_id (e.g. CC-001) is the human-readable key
hqs	Headquarters groups (e.g. HQ-A "Sunshine Learning Group")
officer_hq_assignments	Junction table linking ECDA officers to their assigned HQs
users	Application users; hq_id set for HQ_ADMIN, centre_id set for CENTRE_LEADER
kah_details	KAH history per centre; is_current flags the active record
centre_contacts	Contact records per centre
centre_lifecycle_events	Append-only audit log
waiver_history	Regulatory waiver records per centre


---

Key changes from the previous version:
- Updated seed credentials (`HQAAdmin`, `HQACenterLeader1`) with their scopes
- Roles & Access table now includes a **Visibility Scope** column
- Added `CentreScopeService` to the architecture notes
- Added waiver history and centre-visibility Playwright specs to the project structure
- Added a Database Schema Overview table
- Noted `canViewWaivers` in `AuthContext`
