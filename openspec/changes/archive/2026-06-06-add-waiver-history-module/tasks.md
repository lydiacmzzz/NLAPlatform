## 1. Fix Backend Access Control

- [x] 1.1 In `backend/src/main/kotlin/com/ecda/platform/config/SecurityConfig.kt`, change the waiver endpoint rule from `hasAnyRole("ECDA_OFFICER", "HQ_ADMIN")` to `hasRole("ECDA_OFFICER")`
- [x] 1.2 In `backend/src/test/kotlin/com/ecda/platform/controller/WaiverHistoryControllerTest.kt`, update the `HQ_ADMIN` test to assert 403 (not 200)

## 2. Fix Frontend Permission Gating

- [x] 2.1 In `frontend/src/contexts/AuthContext.tsx`, add `canViewWaivers: boolean` to the `AuthContextValue` interface and compute it as `user !== null && user.role === 'ECDA_OFFICER'`
- [x] 2.2 In `frontend/src/pages/CentreDetailPage.tsx`, destructure `canViewWaivers` from `useAuth()` and replace `canEdit` with `canViewWaivers` in both the `useWaiverHistory` call and the `WaiverHistory` render guard

## 3. Verify Tests Pass

- [x] 3.1 Run backend tests: `cd backend && ./gradlew test --tests "*.WaiverHistoryControllerTest"` — all assertions must pass
- [x] 3.2 Run backend tests: `cd backend && ./gradlew test --tests "*.WaiverHistoryServiceTest"` — no regressions
- [x] 3.3 Run frontend tests: `cd frontend && npm test` — `WaiverHistory.test.tsx` and any auth context tests must pass
- [x] 3.4 Run full backend test suite: `cd backend && ./gradlew test` — no regressions
