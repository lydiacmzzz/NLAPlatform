# Centre Profile Agent Guide

Your intelligent assistant for developing the Centre Profile application. This guide helps you navigate common tasks, debug issues, and maintain high code quality.

---

## Quick Navigation

### 🚀 Getting Started
- **First time?** Read [CLAUDE.md](../../../CLAUDE.md) in the project root
- **Project Overview**: Full-stack app for managing licensed childcare centres in Singapore
- **Tech Stack**: React 18 + Kotlin + Spring Boot 3 + PostgreSQL

### 🛠️ Development Setup
```bash
# Frontend
cd frontend
npm install
npm run dev              # http://localhost:5173

# Backend
cd backend
./gradlew bootRun       # http://localhost:8080

# Database (required for backend)
# Ensure PostgreSQL is running locally with pg_hba.conf set to trust
```

---

## Troubleshooting Guide

### Problem: Login is Failing ❌
**When**: User can't log in, sees errors, or gets 401 responses

**Solution**: Use the **`/login-debug`** skill
```
/login-debug
```

This skill will guide you through 9 ordered steps:
1. ✅ Backend bootup check
2. ✅ Database connection
3. ✅ Flyway migrations
4. ✅ Seed data verification
5. ✅ Auth endpoint testing
6. ✅ JWT/token validation
7. ✅ CORS configuration
8. ✅ JWT handling in frontend
9. ✅ UI layer inspection

**Key files involved**:
- Backend: `SecurityConfig.kt`, `JwtAuthFilter.kt`, `AuthController.kt`
- Frontend: `api.ts`, `AuthContext.tsx`, `LoginPage.tsx`
- Database: `application.yml` (connection settings)

**Default Test Credentials**:
```
Username: admin | Password: password | Role: HQ_ADMIN
Username: officer1 | Password: password | Role: ECDA_OFFICER
Username: leader1 | Password: password | Role: CENTRE_LEADER
```

---

### Problem: Tests Are Failing or Unclear ❌
**When**: Unit tests, integration tests, or test strategy questions

**Solution**: Use the **`/test-strategy`** skill
```
/test-strategy
```

This skill covers:
- ✅ Frontend testing (hooks, components, pages with vitest)
- ✅ Backend testing (unit, slice, integration with JUnit + MockK)
- ✅ Test organization & naming conventions
- ✅ Running tests for both layers
- ✅ Coverage goals & reports
- ✅ Common pitfalls & debugging

**Quick Commands**:
```bash
# Frontend
cd frontend && npm test                    # Run all tests
npm test -- --watch                       # Watch mode
npm run test:coverage                     # Coverage report

# Backend
cd backend && ./gradlew test               # Run all tests
./gradlew test --tests "*.TestName"       # Specific test
./gradlew jacocoTestReport                # Coverage report
```

---

### Problem: Need Project Context 📚
**When**: Understanding architecture, conventions, or database schema

**Solution**: Reference **[CLAUDE.md](../../../CLAUDE.md)**
- Backend layer structure (service → controller pattern)
- Database schema & key relationships
- Auth flow & role-based access
- Code conventions for Kotlin & React
- Common pitfalls & anti-patterns

**Key Architecture Points**:
```
Frontend: pages → hooks → services → axios (api.ts)
Backend: controller → service → repository
Auth: JWT token in localStorage, attached via interceptor
Database: PostgreSQL with Flyway migrations + H2 for tests
```

---

## Common Development Tasks

### 🔑 Implementing a New Feature
1. **Plan**: Use `writing-plans` skill to outline steps
2. **Design**: Use `brainstorming` skill to explore requirements
3. **Implement**: Follow project conventions from CLAUDE.md
4. **Test**: Use `test-driven-development` skill
5. **Review**: Use `requesting-code-review` skill before merging

### 🐛 Debugging a Bug
1. **Isolate**: Use `systematic-debugging` skill
2. **Login Issue?** → Run `/login-debug`
3. **Test Failure?** → Run `/test-strategy`
4. **Other?** → Check CLAUDE.md common pitfalls section

### ✅ Verifying Your Work
1. **Before claiming completion**: Use `verification-before-completion` skill
2. **Code review**: Use `requesting-code-review` or `code-review` skill
3. **Integration test**: Use `verify` skill to run the app manually

### 🌳 Managing Branches
- **New feature work?** → Use `using-git-worktrees` skill for isolation
- **Finishing up?** → Use `finishing-a-development-branch` skill

---

## Project Structure Reference

```
intelligent-task-manager/
├── CLAUDE.md                          # Project documentation (READ THIS FIRST)
├── frontend/                          # React 18 + TypeScript + Vite
│   ├── src/
│   │   ├── pages/                    # Page components
│   │   ├── components/               # Reusable components
│   │   ├── hooks/                    # Custom hooks (data fetching)
│   │   ├── services/                 # API services
│   │   ├── context/                  # Auth context
│   │   └── __tests__/                # Test files
│   ├── package.json
│   └── vitest.config.ts
├── backend/                           # Kotlin + Spring Boot 3
│   ├── src/main/kotlin/
│   │   ├── controller/               # HTTP endpoints
│   │   ├── service/                  # Business logic
│   │   ├── model/                    # JPA entities
│   │   ├── dto/                      # Request/response DTOs
│   │   ├── repository/               # Spring Data JPA
│   │   └── security/                 # Auth, JWT, filters
│   ├── src/test/kotlin/              # Test files
│   └── build.gradle.kts
└── .claude/
    ├── CLAUDE.md                     # Project instructions
    ├── skills/                       # Custom skills
    │   ├── login-debug/              # Login debugging guide
    │   ├── test-strategy/            # Testing guide
    │   └── ...
    └── agents/                       # Agent guides (this file)
```

---

## Key Files by Concern

| I Want to... | Check This File |
|---|---|
| Understand project setup | `CLAUDE.md` → Project Overview section |
| Implement auth feature | `CLAUDE.md` → Auth Flow section |
| Debug login issues | `/login-debug` skill |
| Write/fix tests | `/test-strategy` skill |
| Understand database | `CLAUDE.md` → Database Schema section |
| Add API endpoint | `backend/src/main/kotlin/.../controller/` |
| Modify frontend page | `frontend/src/pages/` |
| Check code conventions | `CLAUDE.md` → Code Conventions section |
| Avoid common mistakes | `CLAUDE.md` → Common Pitfalls section |

---

## Decision Tree

```
I'm working on...
│
├─ 🔐 Login/Auth issues?
│  └─ Run: /login-debug
│
├─ ✅ Tests/Test coverage?
│  └─ Run: /test-strategy
│
├─ 🚀 New feature implementation?
│  ├─ Start with: /brainstorming
│  ├─ Plan with: /writing-plans
│  ├─ Test with: /test-driven-development
│  └─ Review with: /requesting-code-review
│
├─ 🐛 Other bugs/issues?
│  └─ Start with: /systematic-debugging
│
├─ 📚 Understanding project structure/conventions?
│  └─ Read: CLAUDE.md
│
└─ ✨ Completing/merging work?
   ├─ Verify with: /verification-before-completion
   ├─ Review with: /code-review or /requesting-code-review
   └─ Finish with: /finishing-a-development-branch
```

---

## Quick Reference Commands

### Frontend
```bash
cd frontend

npm install              # Install dependencies
npm run dev             # Start dev server (http://localhost:5173)
npm run build           # Type-check + build
npm test                # Run tests (watch mode)
npm run test:coverage   # Generate coverage report
npm run lint            # Check for linting errors
npm run lint -- --fix   # Auto-fix linting errors
```

### Backend
```bash
cd backend

./gradlew bootRun                              # Start Spring Boot
./gradlew test                                 # Run all tests
./gradlew test --tests "*.TestName"           # Run specific test class
./gradlew test --tests "*.TestName.method*"   # Run specific test method
./gradlew build                                # Build JAR
./gradlew jacocoTestReport                    # Generate coverage report
```

### Database
```bash
psql -U postgres -d postgres                  # Connect to PostgreSQL
SELECT * FROM users;                          # Check seed users
SELECT version, description, success FROM flyway_schema_history;  # Check migrations
```

---

## Role-Based Access

| Role | Can Read | Can Write | Notes |
|---|---|---|---|
| ECDA_OFFICER | ✅ All centres | ✅ All centres | Government officer |
| HQ_ADMIN | ✅ All centres | ✅ All centres | Head office admin |
| CENTRE_LEADER | ✅ Own centre only | ❌ Read-only | Centre leader |

All roles derive from JWT `role` claim. Check `SecurityConfig.kt` for enforcement.

---

## Common Workflows

### Fixing a Login Bug
1. Run `/login-debug`
2. Follow the 9-step checklist
3. Identify which step is failing
4. Fix in the appropriate file (backend security, database, frontend config)
5. Test with curl or browser
6. Once fixed, update any related tests

### Adding a New Endpoint
1. Run `/brainstorming` to explore design
2. Run `/writing-plans` to outline steps
3. Create DTOs in `backend/.../dto/`
4. Create service method in `CentreService.kt`
5. Create controller endpoint in `CentreController.kt`
6. Write tests using `/test-strategy`
7. Update frontend hook/service to call new endpoint
8. Run `/requesting-code-review` before merging

### Refactoring a Component
1. Ensure tests pass: `npm test`
2. Make changes
3. Run `npm test` again
4. Verify UI still works: `npm run dev` + browser
5. Run `/code-review` for quality feedback
6. Submit PR with `/requesting-code-review`

---

## Getting Help

### For Login Issues
```
/login-debug
```
Provides a 9-step ordered checklist covering backend, database, auth, CORS, JWT, and frontend.

### For Test Questions
```
/test-strategy
```
Covers frontend testing (vitest), backend testing (JUnit + MockK), organization, naming, coverage goals.

### For Project Context
Read: **`CLAUDE.md`** in the project root
- Architecture overview
- Command reference
- Code conventions
- Common pitfalls

### For Feature Planning
```
/brainstorming
```
Explore requirements before implementation.

### For Code Review
```
/requesting-code-review
```
Get feedback on your changes before merging.

---

## Notes

- **Never edit Flyway migrations** after they've been applied — always create new ones
- **All tests use H2 in-memory database** — no real PostgreSQL needed for testing
- **Frontend validation must mirror backend validation** — keep them in sync
- **JWT tokens are stored in localStorage** under key `ecda_token`
- **All repository access goes through CentreService** — never call repos from controllers
- **Every state-changing operation must record a CentreLifecycleEvent** — audit trail

---

**Last Updated**: 2026-05-31  
**Project**: Centre Profile Information (Full-stack React + Kotlin)  
**Questions?** Check CLAUDE.md first, then refer to the appropriate skill above.
