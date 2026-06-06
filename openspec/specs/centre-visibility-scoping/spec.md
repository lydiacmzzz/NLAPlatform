# Centre Visibility Scoping

## Purpose

This capability covers HQ-based row-level access control for all centre-related API endpoints. It defines how each user role's visibility is scoped to specific centres, enforced server-side in the backend service layer.

## Requirements

### Requirement: Each centre belongs to exactly one HQ
The system SHALL associate every centre with a Headquarters (`hq`) record. A centre without an assigned HQ SHALL NOT be accessible via any API endpoint.

#### Scenario: Centre has an assigned HQ
- **WHEN** a centre record exists in the database
- **THEN** it has a non-null `hq_id` referencing a row in the `hqs` table

### Requirement: ECDA Officer sees only centres in their assigned HQs
The system SHALL restrict an `ECDA_OFFICER` user's visibility to centres whose `hq_id` is in the officer's set of assigned HQs (via the `officer_hq_assignments` junction table). Requests for centres outside this set SHALL return 403.

#### Scenario: Officer retrieves centre list — only assigned-HQ centres returned
- **WHEN** an `ECDA_OFFICER` with assignments to HQ-A and HQ-B calls `GET /api/centres`
- **THEN** the response contains only centres belonging to HQ-A and HQ-B

#### Scenario: Officer retrieves a centre in their assigned HQs
- **WHEN** an `ECDA_OFFICER` calls `GET /api/centres/{id}` for a centre in one of their assigned HQs
- **THEN** the response returns 200 with the centre profile

#### Scenario: Officer is denied a centre outside their assigned HQs
- **WHEN** an `ECDA_OFFICER` calls `GET /api/centres/{id}` for a centre in an HQ they are NOT assigned to
- **THEN** the response returns 403 Forbidden

#### Scenario: Officer with no HQ assignments sees empty list
- **WHEN** an `ECDA_OFFICER` with no HQ assignments calls `GET /api/centres`
- **THEN** the response returns 200 with an empty array

### Requirement: HQ Admin sees only centres under their own HQ
The system SHALL restrict an `HQ_ADMIN` user's visibility to centres whose `hq_id` matches the admin's own `hq_id`. Requests for centres belonging to a different HQ SHALL return 403.

#### Scenario: Admin retrieves centre list — only own-HQ centres returned
- **WHEN** an `HQ_ADMIN` assigned to HQ-A calls `GET /api/centres`
- **THEN** the response contains only centres belonging to HQ-A

#### Scenario: Admin retrieves a centre in their own HQ
- **WHEN** an `HQ_ADMIN` calls `GET /api/centres/{id}` for a centre in their own HQ
- **THEN** the response returns 200 with the centre profile

#### Scenario: Admin is denied a centre in a different HQ
- **WHEN** an `HQ_ADMIN` calls `GET /api/centres/{id}` for a centre belonging to a different HQ
- **THEN** the response returns 403 Forbidden

### Requirement: Centre Leader sees only their own centre
The system SHALL restrict a `CENTRE_LEADER` user's visibility to the single centre referenced by the user's `centre_id`. Requests for any other centre SHALL return 403.

#### Scenario: Leader retrieves centre list — only their centre returned
- **WHEN** a `CENTRE_LEADER` linked to CC-001 calls `GET /api/centres`
- **THEN** the response contains exactly one centre: CC-001

#### Scenario: Leader retrieves their own centre profile
- **WHEN** a `CENTRE_LEADER` calls `GET /api/centres/{id}` for their own centre
- **THEN** the response returns 200 with the centre profile

#### Scenario: Leader is denied any other centre
- **WHEN** a `CENTRE_LEADER` calls `GET /api/centres/{id}` for a centre that is not their own
- **THEN** the response returns 403 Forbidden

#### Scenario: Leader with no centre assigned sees empty list
- **WHEN** a `CENTRE_LEADER` with no `centre_id` set calls `GET /api/centres`
- **THEN** the response returns 200 with an empty array

### Requirement: Scope enforcement applies to all centre sub-resources
The system SHALL apply the same scoping rules to all sub-resource endpoints of a centre: KAH history (`/kah`), contacts (embedded in profile), lifecycle events (embedded in profile). An out-of-scope centre SHALL return 403 on all its sub-resource endpoints.

#### Scenario: Officer denied KAH history for out-of-scope centre
- **WHEN** an `ECDA_OFFICER` calls `GET /api/centres/{id}/kah` for a centre outside their HQ assignments
- **THEN** the response returns 403 Forbidden

#### Scenario: Admin denied KAH history for centre in different HQ
- **WHEN** an `HQ_ADMIN` calls `GET /api/centres/{id}/kah` for a centre in a different HQ
- **THEN** the response returns 403 Forbidden

#### Scenario: Leader denied KAH history for a different centre
- **WHEN** a `CENTRE_LEADER` calls `GET /api/centres/{id}/kah` for a centre that is not their own
- **THEN** the response returns 403 Forbidden

### Requirement: Scope enforcement applies to write operations
The system SHALL apply scoping rules to all write endpoints (`POST /api/centres/{id}/*`, `PATCH /api/centres/{id}`, `PATCH /api/centres/{id}/kah/{kahId}`). A caller with write permission by role but whose target centre is outside their scope SHALL receive 403.

#### Scenario: Officer denied update on out-of-scope centre
- **WHEN** an `ECDA_OFFICER` calls `PATCH /api/centres/{id}` for a centre outside their HQ assignments
- **THEN** the response returns 403 Forbidden

#### Scenario: Admin denied update on centre in different HQ
- **WHEN** an `HQ_ADMIN` calls `PATCH /api/centres/{id}` for a centre in a different HQ
- **THEN** the response returns 403 Forbidden

### Requirement: Scope is enforced in the backend service layer, not frontend only
The system SHALL perform all scope checks server-side. A correctly authenticated JWT for any role SHALL NOT bypass scoping by calling API endpoints directly.

#### Scenario: Direct API call respects scope
- **WHEN** a valid JWT is used to call a centre endpoint directly (bypassing the frontend)
- **THEN** the response is subject to the same scoping rules as if called through the UI

### Requirement: Seed data provides deterministic scoping for tests
The system SHALL include seed data defining: 3 HQs (HQ-A "Sunshine Learning Group", HQ-B "BrightPath Education", HQ-C "Hearts & Stars Care"), 6 centres distributed 2 per HQ (CC-001/CC-002 → HQ-A; CC-003/CC-004 → HQ-B; CC-005/CC-006 → HQ-C), `officer1` assigned to HQ-A and HQ-B (sees 4 centres), `HQAAdmin` assigned to HQ-A only (sees 2 centres), `HQACenterLeader1` linked to CC-001 (sees 1 centre). The V7 migration renames the pre-seeded `admin` user to `HQAAdmin` and `leader1` to `HQACenterLeader1`.

#### Scenario: officer1 sees exactly 4 centres
- **WHEN** `officer1` (ECDA_OFFICER, assigned HQ-A and HQ-B) calls `GET /api/centres`
- **THEN** the response contains exactly 4 centres: CC-001, CC-002, CC-003, CC-004

#### Scenario: HQAAdmin sees exactly 2 centres
- **WHEN** `HQAAdmin` (HQ_ADMIN, assigned to HQ-A) calls `GET /api/centres`
- **THEN** the response contains exactly 2 centres: CC-001, CC-002

#### Scenario: HQACenterLeader1 sees exactly 1 centre
- **WHEN** `HQACenterLeader1` (CENTRE_LEADER, linked to CC-001) calls `GET /api/centres`
- **THEN** the response contains exactly 1 centre: CC-001
