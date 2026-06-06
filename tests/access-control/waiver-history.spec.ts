import { test, expect, APIRequestContext } from '@playwright/test';

// Seeded centre CC-001 (auto-increment PK = 1) has waiver history records
const CENTRE_ID = 1;
const WAIVER_ENDPOINT = `/api/centres/${CENTRE_ID}/waivers`;

// Seeded test users — all share the same password (V1 + V2 migrations)
const USERS = {
  ECDA_OFFICER:  { username: 'officer1',         password: 'password' },
  HQ_ADMIN:      { username: 'HQAAdmin',          password: 'password' },
  CENTRE_LEADER: { username: 'HQACenterLeader1',  password: 'password' },
} as const;

// Waiver payload fields that must never appear in a 403 response
const WAIVER_FIELDS = [
  'waiverType',
  'waiverTitle',
  'waiverDescription',
  'waiverStatus',
  'approvalDate',
  'expiryDate',
  'approvedBy',
  'officerRemarks',
  'supportingDocumentName',
];

async function getToken(request: APIRequestContext, username: string, password: string): Promise<string> {
  const res = await request.post('/api/auth/login', {
    data: { username, password },
  });
  expect(res.status(), `Login failed for ${username}`).toBe(200);
  const body = await res.json();
  expect(body.token, `No token returned for ${username}`).toBeTruthy();
  return body.token as string;
}

// ──────────────────────────────────────────────────────────────────────────────
// ECDA_OFFICER — should get 200 with waiver data
// ──────────────────────────────────────────────────────────────────────────────
test('ECDA_OFFICER can read waiver history (200)', async ({ request }) => {
  const token = await getToken(request, USERS.ECDA_OFFICER.username, USERS.ECDA_OFFICER.password);

  const res = await request.get(WAIVER_ENDPOINT, {
    headers: { Authorization: `Bearer ${token}` },
  });

  expect(res.status()).toBe(200);

  const body = await res.json();
  expect(Array.isArray(body)).toBe(true);
  expect(body.length).toBeGreaterThan(0);

  const first = body[0];
  expect(first).toHaveProperty('id');
  expect(first).toHaveProperty('waiverType');
  expect(first).toHaveProperty('waiverTitle');
  expect(first).toHaveProperty('waiverStatus');
  // Confirm actual waiver data is present, not just empty shells
  expect(typeof first.waiverType).toBe('string');
  expect(first.waiverType.length).toBeGreaterThan(0);
});

// ──────────────────────────────────────────────────────────────────────────────
// HQ_ADMIN — should be denied (403) per the access control matrix
// ──────────────────────────────────────────────────────────────────────────────
test('HQ_ADMIN cannot access waiver history (403)', async ({ request }) => {
  const token = await getToken(request, USERS.HQ_ADMIN.username, USERS.HQ_ADMIN.password);

  const res = await request.get(WAIVER_ENDPOINT, {
    headers: { Authorization: `Bearer ${token}` },
  });

  expect(res.status()).toBe(403);

  const rawBody = await res.text();
  for (const field of WAIVER_FIELDS) {
    expect(rawBody, `Response must not leak "${field}" to HQ_ADMIN`).not.toContain(field);
  }
});

// ──────────────────────────────────────────────────────────────────────────────
// CENTRE_LEADER — should be denied (403) per the access control matrix
// ──────────────────────────────────────────────────────────────────────────────
test('CENTRE_LEADER cannot access waiver history (403)', async ({ request }) => {
  const token = await getToken(request, USERS.CENTRE_LEADER.username, USERS.CENTRE_LEADER.password);

  const res = await request.get(WAIVER_ENDPOINT, {
    headers: { Authorization: `Bearer ${token}` },
  });

  expect(res.status()).toBe(403);

  const rawBody = await res.text();
  for (const field of WAIVER_FIELDS) {
    expect(rawBody, `Response must not leak "${field}" to CENTRE_LEADER`).not.toContain(field);
  }
});

// ──────────────────────────────────────────────────────────────────────────────
// Unauthenticated — no token should yield 401 (guard against open endpoint)
// ──────────────────────────────────────────────────────────────────────────────
test('Unauthenticated request is rejected (401)', async ({ request }) => {
  const res = await request.get(WAIVER_ENDPOINT);
  expect(res.status()).toBe(401);
});
