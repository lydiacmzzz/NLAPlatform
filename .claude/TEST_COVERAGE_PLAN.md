# Test Coverage Improvement Plan

Based on the `test-strategy` skill analysis, this document outlines gaps in test coverage and improvements needed for the intelligent-task-manager project.

**Generated**: 2026-05-31  
**Target Coverage**: 70% overall (Frontend 70%, Backend 75%)

---

## Current Test Status

### Frontend Tests ✓
**Location**: `frontend/src/test/`

| Component/Hook | Status | Coverage | Priority |
|---|---|---|---|
| InlineEdit.tsx | ✅ Tested | ~90% | - |
| LicenceStatusBadge.tsx | ✅ Tested | ~85% | - |
| CentreContacts.tsx | ✅ Tested | ~80% | - |
| CentreLifecycle.tsx | ✅ Tested | ~75% | - |
| LoadingSpinner.tsx | ❌ Missing | 0% | HIGH |
| ErrorMessage.tsx | ❌ Missing | 0% | HIGH |
| CentreSearch.tsx | ❌ Missing | 0% | HIGH |
| CentreList.tsx | ❌ Missing | 0% | HIGH |
| CentreProfile.tsx | ❌ Missing | 0% | MEDIUM |
| KAHDetails.tsx | ❌ Missing | 0% | MEDIUM |
| useCentre.ts (hook) | ❌ Missing | 0% | HIGH |
| useCentreSearch.ts (hook) | ❌ Missing | 0% | HIGH |

### Backend Tests ✓
**Location**: `backend/src/test/kotlin/`

| Class | Status | Coverage | Priority |
|---|---|---|---|
| CentreServiceTest.kt | ✅ Tested | ~60% | - |
| AuthServiceTest.kt | ✅ Tested | ~65% | - |
| CentreControllerTest.kt | ✅ Tested | ~50% | MEDIUM |
| KahDetailRepository | ❌ Missing | 0% | MEDIUM |
| CentreContactRepository | ❌ Missing | 0% | MEDIUM |
| CentreLifecycleEvent | ❌ Missing | 0% | LOW |
| JwtAuthFilterTest | ❌ Missing | 0% | HIGH |
| SecurityConfigTest | ❌ Missing | 0% | MEDIUM |

---

## Phase 1: Critical Gaps (High Priority)

### Frontend - Common Components (HIGH)
These are reusable components used across the app. Missing tests here affect all features.

#### 1. LoadingSpinner.tsx
- Status: ❌ Missing
- Purpose: Show loading state during async operations
- Coverage Goal: 100% (simple component)
- Key Scenarios:
  - Renders spinner when visible
  - Hides when isLoading is false
  - Displays optional message

#### 2. ErrorMessage.tsx
- Status: ❌ Missing
- Purpose: Display error messages to users
- Coverage Goal: 100% (simple component)
- Key Scenarios:
  - Renders error text
  - Shows error icon
  - Dismissible (if applicable)
  - Different severity levels (error, warning)

#### 3. CentreSearch.tsx
- Status: ❌ Missing
- Purpose: Search & filter centres
- Coverage Goal: 80%
- Key Scenarios:
  - Filters by centre name
  - Filters by status
  - Pagination works
  - Calls search API with debouncing
  - Displays results

### Frontend - Hooks (HIGH)
These handle all data fetching logic. Critical for app functionality.

#### 4. useCentre.ts
- Status: ❌ Missing
- Purpose: Fetch single centre by ID
- Coverage Goal: 85%
- Key Scenarios:
  - Fetches centre on mount
  - Refetch on manual call
  - Handles loading state
  - Handles error state (404, 500)
  - Handles success state
  - Caches data (if using React Query)

#### 5. useCentreSearch.ts
- Status: ❌ Missing
- Purpose: Search centres with filters
- Coverage Goal: 85%
- Key Scenarios:
  - Searches with filters
  - Paginates results
  - Handles loading/error states
  - Debounces search input
  - Clears results when reset

### Backend - Security (HIGH)
Authentication and authorization are critical to app security.

#### 6. JwtAuthFilterTest.kt
- Status: ❌ Missing
- Purpose: Test JWT token validation
- Coverage Goal: 85%
- Key Scenarios:
  - Valid token passes through
  - Invalid token rejected with 401
  - Expired token rejected
  - Missing token in Authorization header
  - Malformed token header

---

## Phase 2: Important Gaps (Medium Priority)

### Frontend - Page Components
#### 7. CentreProfile.tsx
- Status: ❌ Missing
- Purpose: Display centre details with editing
- Coverage Goal: 60%
- Key Scenarios:
  - Loads centre data
  - Shows editable fields
  - Can only edit if hasPermission
  - Saves updates
  - Shows validation errors

#### 8. CentreList.tsx
- Status: ❌ Missing
- Purpose: Display list of centres
- Coverage Goal: 60%
- Key Scenarios:
  - Renders centre list
  - Pagination works
  - Clicking centre navigates to profile
  - Empty state shown if no results

#### 9. KAHDetails.tsx
- Status: ❌ Missing
- Purpose: Display KAH rotation history
- Coverage Goal: 70%
- Key Scenarios:
  - Shows current KAH
  - Shows KAH history
  - Marks current vs past KAH

### Backend - Repositories & Security

#### 10. KahDetailRepository Tests
- Status: ❌ Missing
- Purpose: Test KAH data access
- Coverage Goal: 70%
- Key Scenarios:
  - Find current KAH for centre
  - Find KAH history
  - Update KAH status

#### 11. SecurityConfigTest.kt
- Status: ❌ Missing
- Purpose: Test Spring Security configuration
- Coverage Goal: 75%
- Key Scenarios:
  - Public endpoints accessible without auth
  - Protected endpoints require auth
  - CORS configuration correct
  - Role-based access working

#### 12. CentreControllerTest.kt (Expand)
- Current Status: ✅ Partial
- Goal: Increase from 50% to 80%
- Missing Scenarios:
  - Pagination tests
  - Filter/search tests
  - Error handling (validation errors)
  - Role-based access control

---

## Phase 3: Additional Coverage (Medium Priority)

### Backend - Additional Services

#### 13. AuthService Edge Cases
- Error handling tests
- Token refresh tests
- Password validation tests
- User role authorization tests

#### 14. Service Layer - Comprehensive
- KAH rotation logic tests
- Lifecycle event recording tests
- Data validation tests
- Concurrent update handling

---

## Test Coverage Goals by Layer

### Frontend
```
Hooks:          85%+ (data fetching critical)
Common Comps:   95%+ (reused everywhere)
Page Comps:     60%+ (integration with hooks)
───────────────────
Overall:        ~70%
```

### Backend
```
Services:       85%+ (business logic)
Controllers:    75%+ (request handling)
Repositories:   70%+ (DB interactions)
Filters:        85%+ (auth critical)
───────────────────
Overall:        ~75%
```

---

## Implementation Order

### Week 1 (Critical)
1. ✅ LoadingSpinner.tsx test
2. ✅ ErrorMessage.tsx test
3. ✅ useCentre.ts hook test
4. ✅ useCentreSearch.ts hook test
5. ✅ JwtAuthFilterTest.kt

### Week 2 (Important)
6. ✅ CentreSearch.tsx test
7. ✅ SecurityConfigTest.kt
8. ✅ CentreControllerTest.kt (expand)
9. ✅ KahDetailRepository tests

### Week 3 (Additional)
10. ✅ CentreProfile.tsx test
11. ✅ CentreList.tsx test
12. ✅ KAHDetails.tsx test
13. ✅ AuthService edge cases

---

## Test File Templates

### Frontend Hook Test Template
```typescript
import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useMyHook } from '../hooks/useMyHook'

describe('useMyHook', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should fetch data on mount', async () => {
    const { result } = renderHook(() => useMyHook())
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false)
    })
    
    expect(result.current.data).toBeDefined()
  })

  it('should handle error state', async () => {
    mockApi.get.mockRejectedValue(new Error('API Error'))
    
    const { result } = renderHook(() => useMyHook())
    
    await waitFor(() => {
      expect(result.current.error).toBeDefined()
    })
  })
})
```

### Frontend Component Test Template
```typescript
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { MyComponent } from '../components/MyComponent'

describe('MyComponent', () => {
  it('should render correctly', () => {
    render(<MyComponent value="test" />)
    expect(screen.getByText('test')).toBeInTheDocument()
  })

  it('should handle user interaction', async () => {
    const onAction = vi.fn()
    const { user } = render(<MyComponent onAction={onAction} />)
    
    await user.click(screen.getByRole('button'))
    
    expect(onAction).toHaveBeenCalled()
  })
})
```

### Backend Service Test Template
```kotlin
@Test
fun `shouldDoSomethingWhenConditionMet`() {
  val input = TestData()
  every { repository.findById(any()) } returns Optional.of(input)
  
  val result = service.doSomething(1)
  
  assertEquals(expected, result)
  verify { repository.save(any()) }
}
```

---

## Running Tests & Checking Coverage

### Frontend
```bash
# Run all tests
cd frontend && npm test

# Watch mode while developing
npm test -- --watch

# Generate coverage report
npm run test:coverage

# Check specific file coverage
npm test -- InlineEdit.test.tsx --coverage
```

### Backend
```bash
# Run all tests
cd backend && ./gradlew test

# Run specific test class
./gradlew test --tests "*.MyTest"

# Generate coverage report
./gradlew jacocoTestReport

# View report: backend/build/reports/jacoco/test/html/index.html
```

---

## Coverage Report Locations

- **Frontend**: `frontend/coverage/index.html`
- **Backend**: `backend/build/reports/jacoco/test/html/index.html`

Open these in a browser to see detailed coverage by file and line.

---

## Key Principles (from test-strategy skill)

1. **Test Behavior, Not Implementation**
   - Test "when user searches, results appear" (behavior)
   - Not "when state updates, render called" (implementation)

2. **Don't Test Framework**
   - ✅ Test your custom hook logic
   - ❌ Don't test "React renders hooks"

3. **Mock External Dependencies**
   - ✅ Mock API calls in hook tests
   - ❌ Don't hit real backend in unit tests

4. **Unit → Integration → E2E**
   - Unit tests for hooks/services (mocked)
   - Integration tests for components with hooks
   - E2E tests for full user flows (if applicable)

---

## Next Steps

1. Start with Phase 1 (critical gaps)
2. Create test files following templates above
3. Run `npm test:coverage` and `./gradlew jacocoTestReport`
4. Aim for 70%+ overall coverage
5. Review coverage reports weekly

---

## References

- Test Strategy Skill: `.claude/skills/test-strategy/SKILL.md`
- Project CLAUDE.md: Project structure & conventions
- Vitest Docs: https://vitest.dev/
- Testing Library: https://testing-library.com/
