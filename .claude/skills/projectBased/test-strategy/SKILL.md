# Test Strategy Guide

A practical testing strategy for the Centre Profile application (React + Kotlin + PostgreSQL).

---

## Testing Philosophy

**Goal**: Catch bugs before production. Tests are safety nets, not documentation.

**Principle**: Test behaviour, not implementation. A test should break when the feature breaks, not when you refactor.

**Rule**: 
- Write tests for business logic (services, hooks, core functions)
- Write tests for regressions you've encountered
- Don't test UI button clicks unless the click triggers critical logic
- Don't test framework behavior (React rendering, Spring DI) unless you've customized it

---

## Frontend Testing Strategy

### Test Pyramid
```
        UI Integration Tests (10%)
           Component Tests (30%)
              Unit Tests (60%)
```

### What to Test — Frontend

#### 1. **Hooks** (Unit Tests)
Test custom hooks (`useCentre`, `useCentreSearch`, etc.) with mock API responses.

```typescript
// Example: testing useCentre hook
test('useCentre fetches centre on mount', async () => {
  const { result } = renderHook(() => useCentre('CC-001'), {
    wrapper: QueryClientProvider,
  })
  
  await waitFor(() => expect(result.current.data).toBeDefined())
  expect(result.current.data.centreId).toBe('CC-001')
})

test('useCentre handles 404 error', async () => {
  mockApi.get.mockRejectedValue({ status: 404 })
  
  const { result } = renderHook(() => useCentre('INVALID'))
  
  await waitFor(() => expect(result.current.error).toBeDefined())
})
```

**Tool**: `vitest` + `@testing-library/react` + `@testing-library/react-hooks`

#### 2. **Components** (Component Tests)
Test components in isolation with mocked hooks/props.

```typescript
// Example: testing InlineEdit component
test('InlineEdit calls onSave when value changes', async () => {
  const onSave = vi.fn()
  const { user } = render(
    <InlineEdit value="Old" onSave={onSave} />
  )
  
  await user.click(screen.getByText('Old'))
  await user.type(screen.getByRole('textbox'), 'New')
  await user.click(screen.getByText('Save'))
  
  expect(onSave).toHaveBeenCalledWith('New')
})

test('InlineEdit validates input before saving', async () => {
  const onSave = vi.fn()
  const { user } = render(
    <InlineEdit 
      value="test@example.com" 
      validate={(v) => !v.includes('@') ? 'Invalid email' : null}
      onSave={onSave}
    />
  )
  
  await user.click(screen.getByText('test@example.com'))
  await user.type(screen.getByRole('textbox'), 'invalid')
  await user.click(screen.getByText('Save'))
  
  expect(onSave).not.toHaveBeenCalled()
  expect(screen.getByText('Invalid email')).toBeInTheDocument()
})
```

**Tool**: `vitest` + `@testing-library/react` + `@testing-library/user-event`

#### 3. **Pages** (Integration Tests)
Test a full page with real data flow (mocked API).

```typescript
// Example: testing LoginPage
test('LoginPage submits credentials and redirects on success', async () => {
  mockApi.post.mockResolvedValue({
    data: { token: 'jwt-token', role: 'HQ_ADMIN' }
  })
  
  const { user } = render(<LoginPage />)
  
  await user.type(screen.getByLabelText('Email'), 'admin@example.com')
  await user.type(screen.getByLabelText('Password'), 'password')
  await user.click(screen.getByRole('button', { name: /login/i }))
  
  await waitFor(() => {
    expect(mockApi.post).toHaveBeenCalledWith(
      '/api/auth/login',
      { email: 'admin@example.com', password: 'password' }
    )
  })
})
```

### Running Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run specific test file
npm test -- useCentre

# Generate coverage report
npm run test:coverage
```

### Frontend Test Coverage Goals

- **Hooks**: 80%+ coverage (critical data-fetching logic)
- **Components**: 60%+ coverage (common/reusable components)
- **Pages**: 40%+ coverage (integration with real flows)
- **Utilities**: 90%+ coverage (pure functions)

**Target**: 70% overall coverage

---

## Backend Testing Strategy

### Test Types
1. **Unit Tests** — Service methods with mocked repositories
2. **Controller Tests** — HTTP layer with `@WebMvcTest`
3. **Integration Tests** — Full stack with H2 in-memory DB

### What to Test — Backend

#### 1. **Service Layer** (Unit Tests)
Test business logic in isolation. Use MockK to mock repositories.

```kotlin
// Example: testing CentreService
@Test
fun createCentre_savesToRepository() {
  val dto = CreateCentreRequest(
    centreId = "CC-001",
    name = "Test Centre",
    type = CentreType.CHILD_CARE
  )
  
  val saved = service.createCentre(dto)
  
  verify { repository.save(any()) }
  assertThat(saved.centreId).isEqualTo("CC-001")
}

@Test
fun updateCentre_recordsLifecycleEvent() {
  val existing = Centre(id = 1, centreId = "CC-001", name = "Old Name")
  every { repository.findById(1) } returns Optional.of(existing)
  
  service.updateCentre(1, UpdateCentreRequest(name = "New Name"))
  
  verify { eventRepository.save(match { 
    it.centreId == 1 && it.eventType == "UPDATED"
  }) }
}
```

**Tool**: `JUnit 5` + `MockK` + `AssertJ`

#### 2. **Controller Layer** (Slice Tests)
Test HTTP endpoints and request/response mapping.

```kotlin
// Example: testing AuthController
@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {
  @MockkBean
  lateinit var authService: AuthService
  
  @Test
  fun login_withValidCredentials_returns200AndToken() {
    every { authService.login("admin@example.com", "password") } 
      returns JwtResponse(token = "jwt-token", role = "HQ_ADMIN")
    
    mockMvc.post("/api/auth/login") {
      contentType = APPLICATION_JSON
      content = """{"email":"admin@example.com","password":"password"}"""
    }.andExpect {
      status { isOk() }
      jsonPath("$.token") { value(startsWith("jwt")) }
    }
  }
  
  @Test
  fun login_withInvalidCredentials_returns401() {
    every { authService.login("admin@example.com", "wrong") } 
      throws InvalidCredentialsException()
    
    mockMvc.post("/api/auth/login") {
      contentType = APPLICATION_JSON
      content = """{"email":"admin@example.com","password":"wrong"}"""
    }.andExpect {
      status { isUnauthorized() }
    }
  }
}
```

**Tool**: `@WebMvcTest` + `SpringMockK`

#### 3. **Repository Layer** (Integration Tests)
Test database interactions with H2 in-memory DB.

```kotlin
// Example: testing CentreRepository
@DataJpaTest
@ActiveProfiles("test")
class CentreRepositoryTest {
  @Autowired
  lateinit var repository: CentreRepository
  
  @Test
  fun findByCentreId_returnsMatchingCentre() {
    val centre = Centre(centreId = "CC-001", name = "Test")
    repository.save(centre)
    
    val found = repository.findByCentreId("CC-001")
    
    assertThat(found).isNotNull()
    assertThat(found?.name).isEqualTo("Test")
  }
  
  @Test
  fun search_withFilters_appliesAllCriteria() {
    repository.save(Centre(centreId = "CC-001", name = "ABC Centre", type = CentreType.CHILD_CARE))
    repository.save(Centre(centreId = "CC-002", name = "XYZ Centre", type = CentreType.CHILDMINDING))
    
    val results = repository.search("ABC", null, null, Pageable.unpaged())
    
    assertThat(results).hasSize(1)
    assertThat(results.first().centreId).isEqualTo("CC-001")
  }
}
```

**Tool**: `@DataJpaTest` + H2 + `AssertJ`

### Running Backend Tests

```bash
cd backend

# Run all tests (uses H2, no real DB needed)
./gradlew test

# Run specific test class
./gradlew test --tests "*.CentreServiceTest"

# Run specific test method
./gradlew test --tests "*.CentreServiceTest.createCentre*"

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Backend Test Coverage Goals

- **Service Layer**: 85%+ coverage (critical business logic)
- **Controller Layer**: 60%+ coverage (request/response handling)
- **Repository Layer**: 50%+ coverage (DB interactions)

**Target**: 75% overall coverage

---

## Test Organization

### Frontend Structure
```
frontend/src/
├── __tests__/
│   ├── hooks/
│   │   ├── useCentre.test.ts
│   │   └── useCentreSearch.test.ts
│   ├── components/
│   │   ├── common/
│   │   │   └── InlineEdit.test.tsx
│   │   └── pages/
│   │       └── LoginPage.test.tsx
│   └── setup.ts (global mocks)
```

### Backend Structure
```
backend/src/test/kotlin/
├── service/
│   ├── CentreServiceTest.kt
│   └── AuthServiceTest.kt
├── controller/
│   ├── AuthControllerTest.kt
│   └── CentreControllerTest.kt
└── repository/
    └── CentreRepositoryTest.kt
```

---

## Naming Conventions

### Test Names
Use `shouldXWhenY` or `xWhenY` pattern:

```kotlin
// ✅ Good
fun shouldReturnCentreWhenIdExists()
fun shouldThrowNotFoundWhenIdInvalid()
fun shouldRecordLifecycleEventWhenUpdated()

// ❌ Bad
fun testGetCentre()
fun testUpdate()
fun test1()
```

### Test File Names
- Frontend: `ComponentName.test.tsx` or `hookName.test.ts`
- Backend: `ServiceNameTest.kt` or `ControllerNameTest.kt`

---

## Common Pitfalls

### Frontend
1. **Testing implementation details, not behavior**
   - ❌ `expect(setState).toHaveBeenCalledWith(...)`
   - ✅ `expect(screen.getByText('Saved')).toBeInTheDocument()`

2. **Forgetting to mock API calls**
   - ❌ Tests hit real backend
   - ✅ Use `vi.mock()` or MSW to mock API

3. **Not waiting for async operations**
   - ❌ `expect(...).toBeDefined()` immediately
   - ✅ `await waitFor(() => expect(...).toBeDefined())`

### Backend
1. **Mocking too much in service tests**
   - ❌ Mock repository AND all dependencies
   - ✅ Mock only external dependencies (DB, API calls)

2. **Not setting `@ActiveProfiles("test")` on integration tests**
   - ❌ Tests use real PostgreSQL connection
   - ✅ Always use `@ActiveProfiles("test")` for H2

3. **Editing existing Flyway migrations after running tests**
   - ❌ Schema state becomes inconsistent
   - ✅ Add new migrations, never edit applied ones

4. **Testing framework behavior instead of application logic**
   - ❌ `test("Spring saves to DB")` — that's Spring's job
   - ✅ `test("Service records lifecycle event when saved")`

---

## CI/CD Integration

### Pre-commit Checks
Run before pushing:
```bash
cd frontend && npm run build && npm test
cd backend && ./gradlew test
```

### Full Pipeline
```bash
# Frontend
npm run lint
npm test -- --coverage
npm run build

# Backend
./gradlew test
./gradlew build
```

---

## Test Doubles Reference

### Frontend
- **Mock**: `vi.mock('module')`
- **Spy**: `vi.spyOn(object, 'method')`
- **Stub**: `apiMock.get = vi.fn().mockResolvedValue(...)`

### Backend
- **Mock**: `mockk<Class>()`
- **Spy**: `spyk(object)`
- **Stub**: `every { object.method() } returns value`

---

## Debugging Tests

### Frontend
```bash
# Run single test file
npm test -- useCentre

# Run with verbose output
npm test -- --reporter=verbose

# Debug in browser (Node inspector)
node --inspect-brk ./node_modules/vitest/vitest.mjs
```

### Backend
```bash
# Run single test with output
./gradlew test --tests "*.TestName" --info

# Debug with IDE
# Set breakpoint, run test in debug mode (IDE feature)
```

---

## Coverage Reports

### Frontend
```bash
npm run test:coverage
# Open: frontend/coverage/index.html
```

### Backend
```bash
./gradlew jacocoTestReport
# Open: backend/build/reports/jacoco/test/html/index.html
```

---

## Key Files

| Concern | File |
|---|---|
| Frontend test setup | `frontend/src/__tests__/setup.ts` |
| Frontend test config | `frontend/vitest.config.ts` |
| Backend test config | `backend/src/test/resources/application-test.yml` |
| Backend test base class | `backend/src/test/kotlin/TestBase.kt` |
| H2 in-memory DB | `application-test.yml` (auto-configured) |

---

## Quick Reference

| Task | Command |
|------|---------|
| Run all tests (both layers) | `cd frontend && npm test && cd ../backend && ./gradlew test` |
| Run frontend tests only | `cd frontend && npm test` |
| Run backend tests only | `cd backend && ./gradlew test` |
| Run specific test | `./gradlew test --tests "*.TestName"` |
| Generate coverage reports | `npm run test:coverage && ./gradlew jacocoTestReport` |
| Fix lint errors (frontend) | `cd frontend && npm run lint -- --fix` |
