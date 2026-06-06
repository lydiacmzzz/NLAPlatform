import { test, expect, APIRequestContext } from '@playwright/test';

// Seed data PKs (auto-increment, inserted in V3 migration order)
const CENTRE_PK = { CC_001: 1, CC_002: 2, CC_003: 3, CC_004: 4, CC_005: 5, CC_006: 6 };

const USERS = {
  OFFICER:       { username: 'officer1',         password: 'password' },
  HQ_ADMIN:      { username: 'HQAAdmin',          password: 'password' },
  CENTRE_LEADER: { username: 'HQACenterLeader1',  password: 'password' },
} as const;

async function getToken(request: APIRequestContext, username: string, password: string): Promise<string> {
  const res = await request.post('/api/auth/login', { data: { username, password } });
  expect(res.status(), `Login failed for ${username}`).toBe(200);
  const body = await res.json();
  expect(body.token, `No token for ${username}`).toBeTruthy();
  return body.token as string;
}

// ── officer1 (HQ-A + HQ-B → CC-001..CC-004) ──────────────────────────────────

test('officer1: GET /api/centres returns exactly 4 centres (CC-001–CC-004)', async ({ request }) => {
  const token = await getToken(request, USERS.OFFICER.username, USERS.OFFICER.password);

  const res = await request.get('/api/centres', { headers: { Authorization: `Bearer ${token}` } });
  expect(res.status()).toBe(200);

  const body = await res.json();
  expect(body.totalElements).toBe(4);

  const ids: string[] = body.content.map((c: { centreId: string }) => c.centreId);
  expect(ids).toContain('CC-001');
  expect(ids).toContain('CC-002');
  expect(ids).toContain('CC-003');
  expect(ids).toContain('CC-004');
  expect(ids).not.toContain('CC-005');
  expect(ids).not.toContain('CC-006');
});

test('officer1: GET /api/centres/1 (CC-001, in scope) returns 200', async ({ request }) => {
  const token = await getToken(request, USERS.OFFICER.username, USERS.OFFICER.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_001}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(200);

  const body = await res.json();
  expect(body.centreId).toBe('CC-001');
});

test('officer1: GET /api/centres/5 (CC-005, out of scope) returns 403', async ({ request }) => {
  const token = await getToken(request, USERS.OFFICER.username, USERS.OFFICER.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_005}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(403);
});

// ── HQAAdmin (HQ-A → CC-001, CC-002) ─────────────────────────────────────────

test('HQAAdmin: GET /api/centres returns exactly 2 centres (CC-001, CC-002)', async ({ request }) => {
  const token = await getToken(request, USERS.HQ_ADMIN.username, USERS.HQ_ADMIN.password);

  const res = await request.get('/api/centres', { headers: { Authorization: `Bearer ${token}` } });
  expect(res.status()).toBe(200);

  const body = await res.json();
  expect(body.totalElements).toBe(2);

  const ids: string[] = body.content.map((c: { centreId: string }) => c.centreId);
  expect(ids).toContain('CC-001');
  expect(ids).toContain('CC-002');
  expect(ids).not.toContain('CC-003');
});

test('HQAAdmin: GET /api/centres/3 (CC-003, different HQ) returns 403', async ({ request }) => {
  const token = await getToken(request, USERS.HQ_ADMIN.username, USERS.HQ_ADMIN.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_003}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(403);
});

// ── HQACenterLeader1 (CC-001 only) ────────────────────────────────────────────

test('HQACenterLeader1: GET /api/centres returns exactly 1 centre (CC-001)', async ({ request }) => {
  const token = await getToken(request, USERS.CENTRE_LEADER.username, USERS.CENTRE_LEADER.password);

  const res = await request.get('/api/centres', { headers: { Authorization: `Bearer ${token}` } });
  expect(res.status()).toBe(200);

  const body = await res.json();
  expect(body.totalElements).toBe(1);
  expect(body.content[0].centreId).toBe('CC-001');
});

test('HQACenterLeader1: GET /api/centres/2 (CC-002, different centre) returns 403', async ({ request }) => {
  const token = await getToken(request, USERS.CENTRE_LEADER.username, USERS.CENTRE_LEADER.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_002}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(403);
});

// ── Sub-resource access control ───────────────────────────────────────────────

test('officer1: GET /api/centres/5/kah (CC-005, out of scope) returns 403', async ({ request }) => {
  const token = await getToken(request, USERS.OFFICER.username, USERS.OFFICER.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_005}/kah`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(403);
});

test('officer1: GET /api/centres/5/waivers (CC-005, out of scope) returns 403', async ({ request }) => {
  const token = await getToken(request, USERS.OFFICER.username, USERS.OFFICER.password);

  const res = await request.get(`/api/centres/${CENTRE_PK.CC_005}/waivers`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(res.status()).toBe(403);
});
