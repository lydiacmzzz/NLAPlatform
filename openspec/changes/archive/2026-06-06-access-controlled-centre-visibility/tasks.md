## 1. Database Schema — New Migrations

- [x] 1.1 Create `backend/src/main/resources/db/migration/V6__hq_and_scoping.sql`: add `hqs` table (`id BIGSERIAL PK`, `code VARCHAR(20) UNIQUE NOT NULL`, `name VARCHAR(255) NOT NULL`); add nullable `hq_id BIGINT REFERENCES hqs(id)` to `centres`; add nullable `hq_id BIGINT REFERENCES hqs(id)` to `users`; add nullable `centre_id BIGINT REFERENCES centres(id)` to `users`; create `officer_hq_assignments` table (`officer_id BIGINT REFERENCES users(id)`, `hq_id BIGINT REFERENCES hqs(id)`, `PRIMARY KEY (officer_id, hq_id)`)
- [x] 1.2 Create `backend/src/main/resources/db/migration/V7__seed_hq_assignments.sql`: insert 3 HQs (`HQ-A` = "Sunshine Learning Group", `HQ-B` = "BrightPath Education", `HQ-C` = "Hearts & Stars Care"); update centres (`CC-001`, `CC-002` → HQ-A; `CC-003`, `CC-004` → HQ-B; `CC-005`, `CC-006` → HQ-C); add `NOT NULL` constraint on `centres.hq_id`; rename `admin` → `HQAAdmin` and `leader1` → `HQACenterLeader1` (`UPDATE users SET username = 'HQAAdmin' WHERE username = 'admin'` and `UPDATE users SET username = 'HQACenterLeader1' WHERE username = 'leader1'`); set `HQAAdmin.hq_id` → HQ-A; set `HQACenterLeader1.centre_id` → CC-001's PK; insert `officer_hq_assignments` rows for `officer1` → (HQ-A, HQ-B)

## 2. Backend — Data Model

- [x] 2.1 Create `backend/src/main/kotlin/com/ecda/platform/model/Hq.kt`: `@Entity @Table(name="hqs")` with fields `id: Long`, `code: String`, `name: String`
- [x] 2.2 Update `backend/src/main/kotlin/com/ecda/platform/model/Centre.kt`: add `@ManyToOne @JoinColumn(name="hq_id") val hq: Hq` field
- [x] 2.3 Update `backend/src/main/kotlin/com/ecda/platform/model/User.kt`: add nullable `hqId: Long?` and `centreId: Long?` columns (plain Long columns, not JPA associations, to avoid circular fetch issues); add `assignedHqIds: List<Long>` as a transient helper (not persisted)
- [x] 2.4 Create `backend/src/main/kotlin/com/ecda/platform/repository/HqRepository.kt`: `JpaRepository<Hq, Long>` with `findByCode(code: String): Hq?`
- [x] 2.5 Update `backend/src/main/kotlin/com/ecda/platform/repository/UserRepository.kt`: add `findByUsername(username: String): User?` if not already present; add a `@Query` to load an officer's assigned HQ IDs: `@Query("SELECT a.hqId FROM OfficerHqAssignment a WHERE a.officerId = :userId") fun findAssignedHqIds(userId: Long): List<Long>`
- [x] 2.6 Create `backend/src/main/kotlin/com/ecda/platform/model/OfficerHqAssignment.kt`: `@Entity @Table(name="officer_hq_assignments")` with composite key `(officerId: Long, hqId: Long)`

## 3. Backend — Scope Resolution Service

- [x] 3.1 Create `backend/src/main/kotlin/com/ecda/platform/service/CentreScopeService.kt`: extract the calling user from `Authentication`, load their `User` record, and return a sealed `CentreScope` type with three variants: `OfficerScope(hqIds: List<Long>)`, `AdminScope(hqId: Long)`, `LeaderScope(centreId: Long)`
- [x] 3.2 In `CentreScopeService`, implement `fun toSpecification(): Specification<Centre>`: for `OfficerScope` add `WHERE centre.hq.id IN (:hqIds)` predicate; for `AdminScope` add `WHERE centre.hq.id = :hqId`; for `LeaderScope` add `WHERE centre.id = :centreId`
- [x] 3.3 In `CentreScopeService`, implement `fun assertInScope(centreId: Long, scope: CentreScope)`: load the centre's `hq_id` and check against scope; throw `ResponseStatusException(FORBIDDEN)` if not in scope

## 4. Backend — Service Layer Integration

- [x] 4.1 Update `CentreService.searchCentres`: accept `Authentication` parameter, resolve scope via `CentreScopeService`, AND the scope specification into the existing `buildSpec` result before querying
- [x] 4.2 Update `CentreService.getCentre(id)`: accept `Authentication`, resolve scope, call `assertInScope` after fetching the centre
- [x] 4.3 Update `CentreService.getCentreByCentreId`: accept `Authentication`, resolve scope, call `assertInScope` after fetching
- [x] 4.4 Update `CentreService.getKahHistory`: accept `Authentication`, resolve scope, call `assertInScope`
- [x] 4.5 Update `CentreService.addKah` and `CentreService.updateKah`: accept `Authentication`, resolve scope, call `assertInScope`
- [x] 4.6 Update `CentreService.getWaiverHistory`: accept `Authentication`, resolve scope (must be `OfficerScope`), call `assertInScope`; throw 403 if scope is not `OfficerScope`
- [x] 4.7 Update `CentreService.updateCentre`: accept `Authentication`, resolve scope, call `assertInScope`

## 5. Backend — Controller Layer

- [x] 5.1 Update `CentreController`: pass `Authentication auth` to every service call updated in section 4 (for endpoints that don't already take `auth`, add `auth: Authentication` parameter)

## 6. Backend — Tests

- [x] 6.1 Create `backend/src/test/kotlin/com/ecda/platform/service/CentreScopeServiceTest.kt`: unit tests for `toSpecification` predicate generation and `assertInScope` for each of the three scope variants (in-scope → passes, out-of-scope → throws 403)
- [x] 6.2 Update `backend/src/test/kotlin/com/ecda/platform/controller/CentreControllerTest.kt`: add controller slice tests using `@WithMockUser` + mocked `CentreScopeService` to verify that `GET /api/centres/{id}` returns 403 when `assertInScope` throws, and 200 when it passes, for each role
- [x] 6.3 Update `backend/src/test/kotlin/com/ecda/platform/controller/WaiverHistoryControllerTest.kt`: add a test that `ECDA_OFFICER` calling `/waivers` for a centre outside their HQ scope gets 403 (mock `getWaiverHistory` to throw `ResponseStatusException(FORBIDDEN)`)
- [x] 6.4 Update `backend/src/test/kotlin/com/ecda/platform/service/CentreServiceTest.kt`: update existing tests that call `getCentre`, `searchCentres`, etc. to pass a mock `Authentication` and a mocked `CentreScopeService` that returns a permissive scope

## 7. Playwright Tests

- [x] 7.1 Create `tests/access-control/centre-visibility.spec.ts`: helper `getToken(request, username, password)` identical to the one in `waiver-history.spec.ts`; define `USERS = { OFFICER: { username: 'officer1', password: 'password' }, HQ_ADMIN: { username: 'HQAAdmin', password: 'password' }, CENTRE_LEADER: { username: 'HQACenterLeader1', password: 'password' } }`
- [x] 7.2 In `centre-visibility.spec.ts`, add test: `officer1` calls `GET /api/centres` → 200, response array length === 4, all returned centres are CC-001–CC-004
- [x] 7.3 In `centre-visibility.spec.ts`, add test: `officer1` calls `GET /api/centres/{id}` for CC-001 (in scope) → 200
- [x] 7.4 In `centre-visibility.spec.ts`, add test: `officer1` calls `GET /api/centres/{id}` for CC-005 (out of scope) → 403
- [x] 7.5 In `centre-visibility.spec.ts`, add test: `HQAAdmin` calls `GET /api/centres` → 200, response array length === 2 (CC-001, CC-002 only)
- [x] 7.6 In `centre-visibility.spec.ts`, add test: `HQAAdmin` calls `GET /api/centres/{id}` for CC-003 (different HQ) → 403
- [x] 7.7 In `centre-visibility.spec.ts`, add test: `HQACenterLeader1` calls `GET /api/centres` → 200, response array length === 1 (CC-001 only)
- [x] 7.8 In `centre-visibility.spec.ts`, add test: `HQACenterLeader1` calls `GET /api/centres/{id}` for CC-002 (different centre) → 403
- [x] 7.9 In `centre-visibility.spec.ts`, add test: `officer1` calls `GET /api/centres/{id}/kah` for CC-005 (out of scope) → 403
- [x] 7.10 In `centre-visibility.spec.ts`, add test: `officer1` calls `GET /api/centres/{id}/waivers` for CC-005 (out of scope) → 403

## 8. Verification

- [x] 8.1 Run `./gradlew test` in `backend/` — all tests pass (no regressions)
- [x] 8.2 Run `npm test -- --run` in `frontend/` — all 58 tests pass (frontend unchanged)
