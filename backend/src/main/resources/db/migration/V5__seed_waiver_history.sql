-- Seed data: waiver history records linked to existing centres

INSERT INTO waiver_history (centre_id, waiver_type, waiver_title, waiver_description, waiver_status, approval_date, expiry_date, approved_by, officer_remarks, supporting_document_name, supporting_document_url)
VALUES
  -- CC-001: HappySchool@Bishan — approved outdoor play area waiver (active)
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'),
   'Physical Environment',
   'Outdoor Play Area Requirement Waiver',
   'Waiver granted as the centre is located in a high-rise HDB block with no accessible outdoor space. Centre provides equivalent indoor gross motor activity zones.',
   'APPROVED',
   '2022-06-01', '2026-05-31',
   'officer1',
   'Approved subject to quarterly review of indoor gross motor programme. Centre to submit activity schedule each term.',
   'CC001_outdoor_waiver_approval.pdf',
   'https://docs.ecda.gov.sg/waivers/CC001_outdoor_waiver_approval.pdf'),

  -- CC-001: HappySchool@Bishan — expired capacity waiver
  ((SELECT id FROM centres WHERE centre_id = 'CC-001'),
   'Operating Capacity',
   'Temporary Capacity Exception Waiver',
   'Temporary waiver to operate at 110% of licensed capacity during a period of increased demand while a new centre was being set up in the same estate.',
   'EXPIRED',
   '2021-09-01', '2022-02-28',
   'admin',
   'Approved as a one-time measure. Centre reverted to licensed capacity upon expiry. No further extension granted.',
   NULL,
   NULL),

  -- CC-002: HappySchool@Tampines — approved staff waiver
  ((SELECT id FROM centres WHERE centre_id = 'CC-002'),
   'Staffing',
   'Temporary Relief Staff Deployment Waiver',
   'Waiver permitting deployment of unregistered relief staff for up to 30 consecutive days while awaiting ECDA registration approval for three new hires.',
   'APPROVED',
   '2023-11-15', '2024-11-14',
   'officer1',
   'Approved with conditions: relief staff must hold relevant early childhood qualifications. Centre to submit weekly staffing logs.',
   'CC002_relief_staff_waiver.pdf',
   'https://docs.ecda.gov.sg/waivers/CC002_relief_staff_waiver.pdf'),

  -- CC-003: RainbowKids@Jurong — superseded renovation waiver
  ((SELECT id FROM centres WHERE centre_id = 'CC-003'),
   'Physical Environment',
   'Post-Renovation Transition Waiver',
   'Waiver issued to allow continued operations during renovation of main activity hall. Superseded by full compliance upon renovation completion.',
   'SUPERSEDED',
   '2023-01-10', '2023-07-09',
   'admin',
   'Waiver superseded on 2023-05-20 following successful completion of renovation works and re-inspection by ECDA officer. Full compliance confirmed.',
   'CC003_renovation_waiver.pdf',
   'https://docs.ecda.gov.sg/waivers/CC003_renovation_waiver.pdf'),

  -- CC-004: RainbowKids@Woodlands — rejected waiver
  ((SELECT id FROM centres WHERE centre_id = 'CC-004'),
   'Staffing',
   'Reduced Staff-to-Child Ratio Waiver',
   'Application for a permanent waiver to reduce the required staff-to-child ratio from 1:5 to 1:7 for the toddler programme, citing difficulties in recruitment.',
   'REJECTED',
   NULL, NULL,
   'officer1',
   'Rejected. Staffing ratio requirements are a core safety standard and cannot be waived on operational grounds. Centre directed to engage recruitment agency and submit staffing improvement plan.',
   NULL,
   NULL),

  -- CC-005: HappyHearts@Bedok — approved waiver (pre-suspension)
  ((SELECT id FROM centres WHERE centre_id = 'CC-005'),
   'Operating Capacity',
   'Extended Operating Hours Waiver',
   'Waiver to operate until 8:00pm on weekdays and 2:00pm on Saturdays to accommodate parents working irregular hours in the Bedok industrial corridor.',
   'APPROVED',
   '2022-04-20', '2024-04-19',
   'admin',
   'Approved based on demonstrated community need. Centre must ensure adequate staffing for extended hours. Waiver not renewed following suspension of licence in November 2024.',
   'CC005_extended_hours_waiver.pdf',
   'https://docs.ecda.gov.sg/waivers/CC005_extended_hours_waiver.pdf'),

  -- CC-006: HappyHearts@Yishun — approved waiver
  ((SELECT id FROM centres WHERE centre_id = 'CC-006'),
   'Physical Environment',
   'Shared Outdoor Space Usage Waiver',
   'Waiver permitting use of adjacent school field as designated outdoor play area under a facility-sharing agreement with the primary school on the same compound.',
   'APPROVED',
   '2023-08-01', '2028-07-31',
   'officer1',
   'Approved. MOU between centre and primary school verified. ECDA to be notified immediately of any changes to the sharing arrangement.',
   'CC006_shared_outdoor_space_waiver.pdf',
   'https://docs.ecda.gov.sg/waivers/CC006_shared_outdoor_space_waiver.pdf');
