## Context

Currently `CentreService` and `CentreRepository` have no concept of "who is asking". All role enforcement is at the URL level in `SecurityConfig` (e.g. `CENTRE_LEADER` cannot POST), but any authenticated user can read any centre. There is no `HQ` entity, no link between `User` and `HQ` or `Centre`, and no row-level filtering.

The `Centre` JPA entity has no `hq` field. The `User` entity has only `username`, `email`, `role`, `isActive`. The `CentreRepository` implements `JpaSpecificationExecutor<Centre>` which gives us a composable `Specification<Centre>` API — this is the key hook for adding dynamic scope predicates without rewriting the query.

## Goals / Non-Goals

**Goals:**
- Introduce an `Hq` entity and link every centre to an HQ
- Link `HQ_ADMIN` users to one HQ, `ECDA_OFFICER` users to one or more HQs, `CENTRE_LEADER` users to one centre
- Enforce scope at the service layer on all read and write operations involving centre data
- Add seed data that exercises all three scoping rules distinctly
- Add unit tests for scope resolution and controller slice tests for 403 scenarios
- Add Playwright tests that prove data does not leak across scope boundaries

**Non-Goals:**
- No UI changes — scoping is backend-only; the frontend already renders only what the API returns
- No admin screen for managing HQ assignments — assignments are seeded/managed via migrations
- No pagination changes to the centre list — filtering is a predicate, not a post-process
- No caching of scope decisions — scope is resolved on every request from the DB

## Decisions

### Decision: Introduce an `Hq` entity (not a simple `hq_code` varchar on Centre)

A full entity allows future features (HQ-level metadata, per-HQ settings) and makes the join explicit in queries. A plain varchar would require string matching in queries and would not support a junction table for officer assignments.

**Alternative considered**: Store `hq_code` as a varchar on both `Centre` and `User`. Rejected because it makes the officer–HQ many-to-many relationship hard to model cleanly.

### Decision: Officer–HQ assignment via a junction table `officer_hq_assignments`

Officers are typically responsible for multiple HQs. A junction table is the correct relational model for many-to-many.

**Alternative considered**: Store a comma-separated list of HQ codes on the `User` row. Rejected — not queryable with a predicate, violates 1NF.

### Decision: Resolve scope in `CentreService` (not in a Spring Security voter or a database view)

Scope resolution is a business rule ("officer1 is assigned to HQ-A and HQ-B") that lives naturally alongside other business rules in `CentreService`. Doing it in a Spring Security voter would require loading the user's HQ list during every HTTP filter pass; a DB view would be hard to test and would not compose with the existing `Specification` query.

**Alternative considered**: Spring Security `@PreAuthorize` with a SpEL expression calling a `CentrePermissionService`. Rejected — SpEL expressions on data-level predicates are hard to test, hard to read, and don't support list-endpoint filtering (only per-object checks).

**Alternative considered**: Database row-level security (PostgreSQL RLS). Rejected — requires per-user DB connections or a session variable approach; incompatible with the single-user connection pool used by Spring Boot + HikariCP.

### Decision: Scope check method signature — pass `Authentication` down to every service method

`CentreController` already receives `Authentication` as a method parameter for write endpoints. We extend this pattern to all endpoints. The service resolves the user's scope by loading the `User` entity from the DB by username (`auth.name`).

**Alternative considered**: Resolve scope in the controller and pass a `CentreScope` value object to the service. Slightly cleaner (service becomes more unit-testable), but adds boilerplate on every controller method. Deferred — can be refactored later once the pattern stabilises.

### Decision: 403 (not 404) when a centre is outside the caller's scope

Returning 404 ("centre not found") for in-scope-missing vs. out-of-scope-existing centres is a common privacy pattern, but it makes tests ambiguous and violates the principle of least surprise for officers who legitimately mis-type an ID. We return 403 for unauthorised access to a known centre. The centre's *existence* is not a secret within the platform.

**Alternative considered**: Always 404 to avoid disclosing existence. Rejected for this internal tool — all users are authenticated ECDA staff, not anonymous members of the public.

### Decision: Seed data assigns 3 HQs with 2 centres each; officer1 covers 2 HQs, HQAAdmin covers 1 HQ, HQACenterLeader1 is linked to CC-001

This gives deterministic test assertions: `officer1` sees CC-001–CC-004 (4 centres), `HQAAdmin` sees CC-001–CC-002 (2 centres), `HQACenterLeader1` sees only CC-001. The assignment is intentionally asymmetric so tests can distinguish scoping from "see everything". The V7 migration also renames the pre-seeded `admin` user to `HQAAdmin` and `leader1` to `HQACenterLeader1` so usernames reflect their actual role and HQ clearly.

## Risks / Trade-offs

- [Risk: N+1 query when resolving officer HQ list on every request] → Load officer HQ IDs in a single IN-list query using `officer_hq_assignments`; result fits in a list and does not warrant caching
- [Risk: Flyway migration adds NOT NULL column `hq_id` to existing `centres` rows] → Migration must first insert HQ rows, then update centres with their `hq_id`, then add the NOT NULL constraint — split across two statements within the same migration
- [Risk: Existing controller tests that don't pass `Authentication`] → Existing tests use `@WithMockUser` which auto-populates `auth.name`; add a `UserRepository` mock that returns a scope-appropriate user for the test role. Note: Playwright tests must use the new usernames `HQAAdmin` and `HQACenterLeader1` (not the old `admin` / `leader1`) after V7 runs.
- [Risk: `CENTRE_LEADER` scope check is a point lookup (centre_id on user) not a set membership check] → Simple equality predicate; no performance concern

## Migration Plan

1. **V6**: Create `hqs` table; add `hq_id` (nullable) to `centres` and `users`; create `officer_hq_assignments` table; add `centre_id` (nullable) to `users`
2. **V7**: Insert 3 HQs; set `hq_id` on all 6 centres; rename `admin` → `HQAAdmin` and `leader1` → `HQACenterLeader1`; set `hq_id` on `HQAAdmin`, `centre_id` on `HQACenterLeader1`; insert `officer_hq_assignments` rows for `officer1`; apply `NOT NULL` constraint on `centres.hq_id`

Rollback: drop V7 data and V6 schema additions (no data loss — existing centre and user rows retain all current fields).

## Open Questions

- Should `POST /api/centres` (create centre) require the caller to specify which HQ the new centre belongs to, or should it be inferred from the officer's assignments? → **Deferred** — not in scope for this change; create centre is already restricted to `ECDA_OFFICER`/`HQ_ADMIN` and scope checks can be added when that endpoint is revisited.
