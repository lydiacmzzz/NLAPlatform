## Why

Waiver history records are sensitive regulatory data that ECDA Officers process directly; policy restricts visibility to Officers only. The current implementation has a security misconfiguration — the backend allows `HQ_ADMIN` access while the access-control test suite explicitly forbids it — and the frontend gates on the generic `canEdit` flag rather than a waiver-specific permission.

## What Changes

- **BREAKING security fix**: Restrict `GET /api/centres/{centreId}/waivers` to `ECDA_OFFICER` only (remove `HQ_ADMIN` from the `hasAnyRole` rule in `SecurityConfig`)
- Add `canViewWaivers` flag to `AuthContext` (true only for `ECDA_OFFICER`)
- Update `CentreDetailPage` to gate the `WaiverHistory` section on `canViewWaivers`, not `canEdit`
- Update `useWaiverHistory` hook's `enabled` condition to use `canViewWaivers`
- Fix `WaiverHistoryControllerTest`: `HQ_ADMIN` test should assert 403, not 200

## Capabilities

### New Capabilities
- `waiver-history`: Read-only view of a centre's waiver history, accessible to ECDA Officers only. Displays waiver type, title, status (APPROVED / EXPIRED / SUPERSEDED / REJECTED), approval/expiry dates, approved-by officer, remarks, and optional supporting document link.

### Modified Capabilities
<!-- No existing specs to modify — openspec/specs/ is empty -->

## Impact

- `backend/src/main/kotlin/.../config/SecurityConfig.kt` — tighten waiver endpoint role rule
- `backend/src/test/kotlin/.../controller/WaiverHistoryControllerTest.kt` — correct HQ_ADMIN assertion to 403
- `frontend/src/contexts/AuthContext.tsx` — add `canViewWaivers` to context value
- `frontend/src/pages/CentreDetailPage.tsx` — use `canViewWaivers` instead of `canEdit`
- `frontend/src/hooks/useWaiverHistory.ts` — use `canViewWaivers` as `enabled` condition
- No new DB migrations needed (schema and seed data are complete)
