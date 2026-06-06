## Context

Waiver history is partially implemented: the DB schema, backend endpoint, and frontend component all exist. The blocker is an access-control misconfiguration introduced during initial implementation:

- `SecurityConfig` grants `GET /api/centres/*/waivers` to both `ECDA_OFFICER` and `HQ_ADMIN`
- The access-control E2E test suite (`tests/access-control/waiver-history.spec.ts`) asserts `HQ_ADMIN` must receive 403
- The controller unit test contradicts the E2E test by asserting `HQ_ADMIN` receives 200
- The frontend gates the waiver section on `canEdit` (true for both `ECDA_OFFICER` and `HQ_ADMIN`) instead of a waiver-specific flag

No DB changes are needed. The fixes are confined to security config, one context value, and two component references.

## Goals / Non-Goals

**Goals:**
- Restrict waiver endpoint to `ECDA_OFFICER` only, aligning `SecurityConfig` with the E2E test intent
- Expose `canViewWaivers` on `AuthContext` so frontend can gate independently from write permission
- Make all test layers consistent: controller test, E2E access-control test, and component test all agree on the same access matrix
- The `WaiverHistory` UI remains read-only (no add/edit/delete actions)

**Non-Goals:**
- No new API endpoints; the existing `GET /api/centres/{centreId}/waivers` is sufficient
- No pagination on waiver list (typically < 20 records per centre)
- No write path for waivers in this change
- No changes to the `WaiverHistory` component's visual design

## Decisions

### Decision: ECDA_OFFICER-only access (not HQ_ADMIN)

The E2E test in `tests/access-control/` is the authoritative access-control specification. The controller test and `SecurityConfig` are wrong relative to it. We align everything to the E2E test.

**Alternative considered**: Allow `HQ_ADMIN` access (fix E2E test instead). Rejected because the test was written to encode a deliberate policy decision — waivers contain officer remarks and applicant-sensitive details that `HQ_ADMIN` (a system admin role) should not see.

### Decision: Add `canViewWaivers` to AuthContext rather than reusing `canEdit`

`canEdit` means "can write to centre data" and maps to `ECDA_OFFICER | HQ_ADMIN`. `canViewWaivers` means "can read regulated waiver records" and maps to `ECDA_OFFICER` only. These are different concerns; conflating them would require callers to know which roles are which.

**Alternative considered**: Pass a derived boolean directly from the `useAuth()` call site. Rejected because putting the role-to-permission mapping in a component violates the single-source-of-truth principle and creates duplication risk.

### Decision: Fix controller test assertion for HQ_ADMIN (403, not 200)

The unit test must stay consistent with the security config. Changing only SecurityConfig without updating the test would leave a misleading test that documents wrong behaviour.

## Risks / Trade-offs

- [Risk: existing users logged in as HQ_ADMIN see the waiver section disappear on reload] → The section is already gated by `canEdit` so it only shows for HQ_ADMIN if they can edit. With the fix, it disappears for them. No data loss; waivers are read-only.
- [Risk: controller test change could be missed in review] → Tasks spec explicitly lists it as a required step.

## Migration Plan

1. Change `SecurityConfig` — one line change, no migration required
2. Update `AuthContext` — additive change (new property), no breaking change for consumers that don't use it
3. Update `CentreDetailPage` and `useWaiverHistory` — straightforward substitution of `canEdit` → `canViewWaivers`
4. Fix controller test — test-only change

Rollback: revert the `SecurityConfig` line to `hasAnyRole("ECDA_OFFICER", "HQ_ADMIN")`. All other changes are non-breaking.

## Open Questions

None — the E2E test is the authority; no further policy clarification needed.
