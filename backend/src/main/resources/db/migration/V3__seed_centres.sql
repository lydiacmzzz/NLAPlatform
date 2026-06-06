-- Seed data: 6 childcare centres across Singapore

INSERT INTO centres (centre_id, licence_number, name, centre_type, address, postal_code, operating_hours, capacity, licence_status, licence_issue_date, licence_expiry_date, renewal_due_date, application_stage, updated_by)
VALUES
  ('CC-001', 'LIC-2021-0001', 'HappySchool@Bishan',                    'INFANT_CARE',       '10 Tampines Central 1, #01-01',         '529536', '7:00am - 7:00pm', 40,  'ACTIVE',          '2021-03-01', '2026-02-28', '2025-11-28', NULL,                  'admin'),
  ('CC-002', 'LIC-2020-0002', 'HappySchool@Tampines',                  'STUDENT_CARE',      '51 Bishan Street 13, #02-05',           '579799', '7:00am - 9:00pm', 80,  'ACTIVE',          '2020-06-15', '2025-06-14', '2025-03-14', NULL,                  'admin'),
  ('CC-003', 'LIC-2019-0003', 'RainbowKids@Jurong',                    'ANCHOR_OPERATOR',   '30 Jurong East Street 31, #01-10',      '609494', '7:00am - 7:00pm', 120, 'PENDING_RENEWAL', '2019-09-01', '2024-08-31', '2024-05-31', 'Renewal In Progress', 'admin'),
  ('CC-004', 'LIC-2022-0004', 'RainbowKids@Woodlands',                 'PARTNER_OPERATOR',  '900 South Woodlands Drive, #03-02',     '730900', '6:30am - 7:30pm', 60,  'ACTIVE',          '2022-01-10', '2027-01-09', '2026-10-09', NULL,                  'admin'),
  ('CC-005', 'LIC-2018-0005', 'HappyHearts@Bedok',                     'INFANT_CARE',       '418 Bedok North Avenue 2, #01-15',      '460418', '7:00am - 7:00pm', 30,  'SUSPENDED',       '2018-04-20', '2023-04-19', NULL,         'Suspension Review',   'admin'),
  ('CC-006', 'LIC-2023-0006', 'HappyHearts@Yishun',                    'STUDENT_CARE',      '101 Yishun Avenue 5, #02-08',           '760101', '7:00am - 9:00pm', 100, 'ACTIVE',          '2023-07-01', '2028-06-30', '2028-03-30', NULL,                  'admin');


-- KAH details (current KAH per centre)

INSERT INTO kah_details (centre_id, principal_name, nric, email, phone, licence_conditions, appointment_start_date, is_current, pending_approval)
VALUES
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'), 'Mdm Lim Bee Leng',    'S8201234A', 'lim.beeleng@sunshine.edu.sg',    '91234567', NULL,                             '2021-03-01', TRUE,  FALSE),
  ((SELECT id FROM centres WHERE centre_id = 'CC-002'), 'Mr Tan Wei Jie',      'S7809876B', 'tan.weijie@littlestars.edu.sg',  '98765432', NULL,                             '2020-06-15', TRUE,  FALSE),
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'), 'Ms Priya Nair',       'S8534567C', 'priya.nair@brightfutures.edu.sg','87654321', 'Monthly compliance report required', '2019-09-01', TRUE,  FALSE),
  ((SELECT id FROM centres WHERE centre_id = 'CC-004'), 'Mr Rajan Subramaniam','S9012345D', 'rajan.s@rainbowkids.edu.sg',    '91122334', NULL,                             '2022-01-10', TRUE,  FALSE),
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'), 'Mdm Chen Xiu Ying',   'S7723456E', 'chen.xiuying@happyhearts.edu.sg','82233445', 'Operations suspended pending review', '2018-04-20', TRUE,  FALSE),
  ((SELECT id FROM centres WHERE centre_id = 'CC-006'), 'Ms Nurul Ain Binte Hamid', 'S9345678F', 'nurul.ain@futureleaders.edu.sg', '93344556', NULL,                      '2023-07-01', TRUE,  FALSE);


-- Contacts per centre (PRIMARY + HQ_LIAISON)

INSERT INTO centre_contacts (centre_id, contact_type, contact_name, role, email, phone)
VALUES
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'), 'PRIMARY',    'Mdm Lim Bee Leng',         'Principal',         'lim.beeleng@sunshine.edu.sg',      '91234567'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'), 'HQ_LIAISON', 'Mr David Chua',             'Operations Manager','david.chua@sunshine-hq.edu.sg',    '62345678'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-002'), 'PRIMARY',    'Mr Tan Wei Jie',            'Principal',         'tan.weijie@littlestars.edu.sg',    '98765432'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-002'), 'HQ_LIAISON', 'Ms Grace Ong',              'Regional Director', 'grace.ong@littlestars-hq.edu.sg',  '63456789'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'), 'PRIMARY',    'Ms Priya Nair',             'Principal',         'priya.nair@brightfutures.edu.sg',  '87654321'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'), 'HQ_LIAISON', 'Mr Kevin Loh',              'Compliance Officer','kevin.loh@brightfutures-hq.edu.sg','64567890'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-004'), 'PRIMARY',    'Mr Rajan Subramaniam',      'Principal',         'rajan.s@rainbowkids.edu.sg',       '91122334'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-004'), 'EMERGENCY',  'Ms Janet Tan',              'Deputy Principal',  'janet.tan@rainbowkids.edu.sg',     '91122335'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'), 'PRIMARY',    'Mdm Chen Xiu Ying',         'Principal',         'chen.xiuying@happyhearts.edu.sg',  '82233445'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'), 'HQ_LIAISON', 'Mr Alan Wong',              'Legal Counsel',     'alan.wong@happyhearts-hq.edu.sg',  '65678901'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-006'), 'PRIMARY',    'Ms Nurul Ain Binte Hamid',  'Principal',         'nurul.ain@futureleaders.edu.sg',   '93344556'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-006'), 'HQ_LIAISON', 'Mr Faisal Ibrahim',         'Regional Manager',  'faisal.i@futureleaders-hq.edu.sg', '66789012');


-- Lifecycle events

INSERT INTO centre_lifecycle_events (centre_id, event_type, description, occurred_at, recorded_by)
VALUES
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'), 'LICENCE_ISSUED',   'Initial licence issued for HappySchool@Bishan.',                          '2021-03-01 09:00:00+08', 'admin'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-002'), 'LICENCE_ISSUED',   'Initial licence issued for HappySchool@Tampines.',                        '2020-06-15 09:00:00+08', 'admin'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'), 'LICENCE_ISSUED',   'Initial licence issued for RainbowKids@Jurong.',                          '2019-09-01 09:00:00+08', 'admin'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'), 'RENEWAL_INITIATED','Licence renewal application submitted. Current licence expires 2024-08-31.','2024-04-10 10:30:00+08', 'officer1'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-004'), 'LICENCE_ISSUED',   'Initial licence issued for RainbowKids@Woodlands.',                       '2022-01-10 09:00:00+08', 'admin'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'), 'LICENCE_ISSUED',   'Initial licence issued for HappyHearts@Bedok.',                           '2018-04-20 09:00:00+08', 'admin'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'), 'SUSPENSION',       'Licence suspended due to non-compliance with staff-to-child ratio.',       '2024-11-05 14:00:00+08', 'officer1'),
  ((SELECT id FROM centres WHERE centre_id = 'CC-006'), 'LICENCE_ISSUED',   'Initial licence issued for HappyHearts@Yishun.',                          '2023-07-01 09:00:00+08', 'admin');
