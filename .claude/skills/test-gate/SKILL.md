# Test Gate Skill

Purpose:
Prevent commits and pushes with failing code.

Before creating any commit:

1. Run frontend tests
2. Run backend tests
3. Run lint checks
4. Run build verification

Requirements:

- Never create a commit if any test fails
- Never create a commit if build fails
- Fix all issues before committing
- Re-run affected tests after every fix

Verification Checklist:

- Frontend build passes
- Backend build passes
- Unit tests pass
- Integration tests pass (if available)
- Playwright smoke tests pass (if available)

Only proceed with commit when all checks are green.