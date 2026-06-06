-- Insert HQs
INSERT INTO hqs (code, name) VALUES
    ('HQ-A', 'Sunshine Learning Group'),
    ('HQ-B', 'BrightPath Education'),
    ('HQ-C', 'Hearts & Stars Care');

-- Assign centres to HQs
UPDATE centres SET hq_id = (SELECT id FROM hqs WHERE code = 'HQ-A') WHERE centre_id IN ('CC-001', 'CC-002');
UPDATE centres SET hq_id = (SELECT id FROM hqs WHERE code = 'HQ-B') WHERE centre_id IN ('CC-003', 'CC-004');
UPDATE centres SET hq_id = (SELECT id FROM hqs WHERE code = 'HQ-C') WHERE centre_id IN ('CC-005', 'CC-006');

-- Enforce NOT NULL on centres.hq_id now that all rows are populated
ALTER TABLE centres ALTER COLUMN hq_id SET NOT NULL;

-- Rename test users to reflect their HQ/role clearly
UPDATE users SET username = 'HQAAdmin'          WHERE username = 'admin';
UPDATE users SET username = 'HQACenterLeader1'  WHERE username = 'leader1';

-- Assign HQAAdmin to HQ-A
UPDATE users SET hq_id = (SELECT id FROM hqs WHERE code = 'HQ-A') WHERE username = 'HQAAdmin';

-- Assign HQACenterLeader1 to CC-001
UPDATE users SET centre_id = (SELECT id FROM centres WHERE centre_id = 'CC-001') WHERE username = 'HQACenterLeader1';

-- Assign officer1 to HQ-A and HQ-B
INSERT INTO officer_hq_assignments (officer_id, hq_id)
SELECT u.id, h.id FROM users u, hqs h
WHERE u.username = 'officer1' AND h.code IN ('HQ-A', 'HQ-B');
