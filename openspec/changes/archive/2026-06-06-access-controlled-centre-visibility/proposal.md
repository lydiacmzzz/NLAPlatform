## Why

Currently every authenticated user can query any centre — access is gated only by role, not by which HQ or centre the user is responsible for. This means an `HQ_ADMIN` for one operator can see another operator's centres, and an `ECDA_OFFICER` has no assignment boundary. Backend enforcement of data-level scoping is required before the platform can be used across multiple operators.

## What Changes

- **BREAKING (DB schema)**: Add `hqs` table and link centres and users to it
  - New table `hqs` (id, code, name)
  - Add `hq_id` FK to `centres` — each centre belongs to exactly one HQ
  - Add `hq_id` FK to `users` (nullable) — populated for `HQ_ADMIN` users
  - Add `centre_id` FK to `users` (nullable) — populated for `CENTRE_LEADER` users
  - New junction table `officer_hq_assignments` (officer_id, hq_id) — maps `ECDA_OFFICER` users to one or more HQs
- **Backend enforcement** on all centre-related endpoints:
  - `GET /api/centres` — results filtered to the caller's visible scope
  - `GET /api/centres/{id}` and `/by-centre-id/{centreId}` — 403 if outside caller's scope
  - `GET /api/centres/{centreId}/kah`, `POST`, `PATCH` — 403 if outside caller's scope
  - `GET /api/centres/{centreId}/waivers` — 403 if outside officer's assigned HQs
  - `POST /api/centres`, `PATCH /api/centres/{id}` — 403 if outside caller's scope
- **Updated seed data** (new Flyway migration): 3 HQs, centres distributed across HQs; rename existing test users (`admin` → `HQAAdmin`, `leader1` → `HQACenterLeader1`) and assign them to their HQ/centre
- **New backend tests**: unit tests for scope-check logic and controller slice tests for each scoping scenario
- **New Playwright tests**: API-level access-control matrix for all scoped endpoints

## Capabilities

### New Capabilities
- `centre-visibility-scoping`: Row-level access control for all centre data. Defines the HQ model, the three-role scoping rules (ECDA_OFFICER → assigned HQs, HQ_ADMIN → own HQ, CENTRE_LEADER → own centre), and backend enforcement requirements for every centre-related endpoint.

### Modified Capabilities
- `waiver-history`: The existing "ECDA Officer retrieves waiver history" scenario must add data-scoping — an officer can only retrieve waivers for centres within their assigned HQs, not all centres system-wide. Returns 403 for centres outside their scope.

## Impact

- **New Flyway migration** (`V6__hq_and_scoping.sql`): schema additions
- **New Flyway migration** (`V7__seed_hq_assignments.sql`): seed HQs, link centres and users; rename `admin` → `HQAAdmin` and `leader1` → `HQACenterLeader1` in the `users` table
- `backend/.../model/Hq.kt` — new JPA entity
- `backend/.../model/User.kt` — add `hqId`, `centreId` fields
- `backend/.../model/Centre.kt` — add `hq` association
- `backend/.../repository/HqRepository.kt` — new repository
- `backend/.../repository/UserRepository.kt` — add lookup by username with scope fields
- `backend/.../service/CentreService.kt` — add scope resolution and enforcement on all methods
- `backend/.../controller/CentreController.kt` — pass `Authentication` to all service methods that need scoping
- New test classes: `CentreVisibilityScopingTest` (unit), controller slice tests for scoped access
- `tests/access-control/centre-visibility.spec.ts` — new Playwright access-control matrix
