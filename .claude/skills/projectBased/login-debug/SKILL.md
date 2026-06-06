# Login Debugging Guide

When login fails in the Centre Profile application, follow this structured debugging checklist to identify the root cause.

**Trigger**: Any login failure — "invalid credentials", 401 errors, redirect loops, blank screen after login, token not set.

**Rule**: Always follow the steps **in order**. Do not skip to the UI until all prior steps are confirmed healthy. Each step has a pass condition — if it fails, fix it before continuing.

---

## Step 1 — Check Backend Bootup

**Goal**: Confirm the Spring Boot process is running and listening.

```bash
# Check if port 8080 is occupied
netstat -ano | findstr :8080

# Or hit the health endpoint
curl http://localhost:8080/actuator/health
```

**Pass**: Port 8080 is bound, or health returns `{"status":"UP"}`.  
**Fail**: Process not found → run `./gradlew bootRun` and watch for startup errors in the console. Look for `APPLICATION FAILED TO START` and read the cause.

---

## Step 2 — Check Database Connection

**Goal**: Confirm the backend can reach PostgreSQL.

Check `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    # no password — pg_hba.conf must be set to trust
```

Verify PostgreSQL is running and accepting connections:
```bash
psql -U postgres -c "\l"
```

Check `pg_hba.conf` (typically `C:\Program Files\PostgreSQL\<version>\data\pg_hba.conf`):
- IPv4: `host all all 127.0.0.1/32 trust`
- IPv6: `host all all ::1/128 trust`   ← **easy to miss**

After editing pg_hba.conf, reload PostgreSQL:
```sql
SELECT pg_reload_conf();
```

**Pass**: `psql -U postgres` connects without password prompt.  
**Fail**: `FATAL: password authentication failed` → pg_hba.conf not set to trust, or wrong IP family (127.0.0.1 vs ::1).

---

## Step 3 — Check Migrations

**Goal**: Confirm Flyway applied all migrations cleanly.

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

All rows must have `success = true`. If any row has `success = false`:
1. Fix the failing migration SQL.
2. Delete the failed row from `flyway_schema_history`.
3. Restart the backend — Flyway will retry.

**Never edit a migration that has already been applied successfully.** Add a new migration instead.

**Pass**: All migrations show `success = true`.  
**Fail**: Missing table errors in backend logs → migration didn't run or failed silently.

---

## Step 4 — Check Seed Data

**Goal**: Confirm at least one user exists in the `users` table.

```sql
SELECT username, role, LEFT(password_hash, 10) FROM users;
```

Expected seed users (all password = `password` via BCrypt):
| username | role |
|---|---|
| admin | HQ_ADMIN |
| officer1 | ECDA_OFFICER |
| leader1 | CENTRE_LEADER |

If the table is empty, the seed migration (V1 or V2) may not have run.

**Pass**: At least one user row exists with a non-empty BCrypt hash (starts with `$2a$`).  
**Fail**: No rows → re-check Step 3. The seed is in a Flyway migration, not inserted manually.

---

## Step 5 — Check Auth Middleware

**Goal**: Confirm `POST /api/auth/login` returns a JWT for valid credentials.

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Expected: `{"token":"eyJ..."}` with HTTP 200.

If 401 → `UserDetailsService` cannot find the user, or BCrypt hash mismatch.  
If 500 → check backend logs. Common causes:
- `JwtUtil` secret key too short (must be ≥256 bits for HS256).
- `SecurityConfig` not permitting `/api/auth/login` without auth.

Check `SecurityConfig.kt` — the login endpoint must be in `permitAll()`:
```kotlin
.requestMatchers("/api/auth/login").permitAll()
```

**Pass**: curl returns a JWT string.  
**Fail**: Fix the specific error before continuing.

---

## Step 6 — Check CORS

**Goal**: Confirm the backend allows requests from the frontend origin.

Check `SecurityConfig.kt` for a `CorsConfigurationSource` bean:
```kotlin
config.allowedOrigins = listOf("http://localhost:5173")
config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
config.allowedHeaders = listOf("*")
config.allowCredentials = true
```

Common mistake: origin is `http://localhost:5173` but the frontend runs on a different port.

In the browser DevTools → Network tab, look for a CORS error on the preflight `OPTIONS` request to `/api/auth/login`.

**Pass**: No CORS error in browser console; OPTIONS request returns 200 with `Access-Control-Allow-Origin`.  
**Fail**: Add or correct the allowed origin in `SecurityConfig`.

---

## Step 7 — Check Frontend Environment

**Goal**: Confirm the frontend is pointing at the correct backend URL.

Check `frontend/.env` or `frontend/.env.local`:
```
VITE_API_URL=http://localhost:8080
```

If no `.env` file exists, check `frontend/src/services/api.ts` — the Axios base URL defaults:
```typescript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
})
```

Also confirm the dev server is running (`npm run dev`) and accessible at `http://localhost:5173`.

**Pass**: `VITE_API_URL` matches the running backend, or the default is correct.  
**Fail**: Create/update `.env.local` and restart `npm run dev`.

---

## Step 8 — Check JWT Handling

**Goal**: Confirm the token is stored and sent correctly after login.

After a successful login call, open browser DevTools:
- **Application → Local Storage → http://localhost:5173** → key `ecda_token` should exist with the JWT value.
- **Network tab** → any subsequent API request (e.g. `GET /api/centres`) should have `Authorization: Bearer eyJ...` in its request headers.

Check `frontend/src/services/api.ts` for the interceptor:
```typescript
api.interceptors.request.use(config => {
  const token = localStorage.getItem('ecda_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

Check `frontend/src/context/AuthContext.tsx` — `login()` must call `localStorage.setItem('ecda_token', token)`.

**Pass**: Token is present in localStorage; subsequent requests carry the `Authorization` header.  
**Fail**: Token stored under wrong key, or interceptor not wired, or `AuthContext.login()` not persisting the token.

---

## Step 9 — Inspect UI

**Only reach this step if all prior steps pass.**

**Goal**: Identify any UI-layer issues — form validation, error display, redirect logic.

Checklist:
- Does the login form call the correct service function (not a hardcoded fetch)?
- Is a user-visible error shown on 401 (not just a `console.log`)?
- After successful login, does the router redirect to `/` or `/centres`?
- Does `ProtectedRoute` in `App.tsx` check `AuthContext.isAuthenticated` (derived from token presence)?
- Is the page blank? → open Console tab for runtime JS errors.

Check `frontend/src/pages/LoginPage.tsx` — error state must render a message element, not just log to console.

**Pass**: Login form submits, token is stored, user is redirected to the main page.  
**Fail**: Fix the specific UI/routing issue identified.

---

---

## Quick Troubleshooting Flowchart

```
Login fails?
├─ Browser shows error?
│  ├─ Network error (404/500)? → Check Backend Bootup (#1)
│  ├─ CORS error? → Check CORS (#6)
│  └─ "Invalid credentials"? → Check Seed Data (#4)
│
├─ Backend logs show error?
│  ├─ Database error? → Check Database Connection (#2)
│  ├─ Migration failed? → Check Migrations (#3)
│  ├─ Auth endpoint not found? → Check Auth Middleware (#5)
│  └─ Token creation failed? → Check JWT Handling (#8)
│
└─ No error, page won't load?
   ├─ Check Frontend Environment (#7)
   └─ Check UI Issues (#9)
```

---

## Common Login Failure Scenarios

| Scenario | Root Cause | Fix |
|----------|-----------|-----|
| "404 Not Found" on login | Backend not running | `./gradlew bootRun` |
| "Connection refused" in logs | PostgreSQL down | Start PostgreSQL |
| "User not found" after login | Seed data not created | Check Step 4 (migrations may have failed) |
| CORS error in console | Frontend origin not allowed | Update `SecurityConfig` CORS settings per Step 6 |
| "Invalid token" after login | JWT secret mismatch | Check `application.yml` secret key in Step 5 |
| Login works, but page is blank | Frontend not configured correctly | Check `VITE_API_URL` in Step 7 |
| 401 Unauthorized | Token not sent or expired | Check Step 8 (localStorage & interceptor) |
| Authentication success but redirect fails | Router or ProtectedRoute broken | Check Step 9 |

---

## Quick Reference — Credentials

| Username | Password | Role |
|---|---|---|
| admin | password | HQ_ADMIN (read + write) |
| officer1 | password | ECDA_OFFICER (read + write) |
| leader1 | password | CENTRE_LEADER (read-only) |

## Quick Reference — Key Files

| Concern | File |
|---|---|
| Login endpoint | `backend/.../controller/AuthController.kt` |
| JWT generation | `backend/.../security/JwtUtil.kt` |
| Token filter | `backend/.../security/JwtAuthFilter.kt` |
| Security rules | `backend/.../security/SecurityConfig.kt` |
| Axios instance | `frontend/src/services/api.ts` |
| Auth context | `frontend/src/context/AuthContext.tsx` |
| Login page | `frontend/src/pages/LoginPage.tsx` |
| Seed users | `backend/src/main/resources/db/migration/V1__initial_schema.sql` |

## Debug Commands Reference

```bash
# Backend
cd backend && ./gradlew bootRun                    # Start backend
./gradlew test                                     # Run all tests

# Frontend
cd frontend && npm run dev                         # Start dev server
npm run build                                      # Type-check + build

# Database
psql -U postgres -c "\l"                           # Connect to PostgreSQL
SELECT * FROM users;                               # Check users table
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;  # Check migrations

# Test login endpoint
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```
