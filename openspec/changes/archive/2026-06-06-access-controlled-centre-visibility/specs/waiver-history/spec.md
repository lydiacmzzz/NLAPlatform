## MODIFIED Requirements

### Requirement: Waiver history is accessible only to ECDA Officers within their assigned HQs
The system SHALL restrict access to `GET /api/centres/{centreId}/waivers` to users with the `ECDA_OFFICER` role AND whose HQ assignments include the HQ of the requested centre. All other roles (including `HQ_ADMIN` and `CENTRE_LEADER`) SHALL receive a 403 Forbidden response. An `ECDA_OFFICER` requesting waivers for a centre outside their assigned HQs SHALL also receive 403. Unauthenticated requests SHALL receive 401 Unauthorized.

#### Scenario: ECDA Officer retrieves waiver history for an in-scope centre
- **WHEN** an `ECDA_OFFICER` sends `GET /api/centres/{centreId}/waivers` for a centre in one of their assigned HQs
- **THEN** the system returns 200 with a JSON array of waiver records for that centre

#### Scenario: ECDA Officer is denied waiver history for an out-of-scope centre
- **WHEN** an `ECDA_OFFICER` sends `GET /api/centres/{centreId}/waivers` for a centre NOT in any of their assigned HQs
- **THEN** the system returns 403 Forbidden

#### Scenario: HQ Admin is denied access
- **WHEN** an `HQ_ADMIN` sends `GET /api/centres/{centreId}/waivers` with a valid JWT
- **THEN** the system returns 403 Forbidden and the response body does not contain any waiver field names

#### Scenario: Centre Leader is denied access
- **WHEN** a `CENTRE_LEADER` sends `GET /api/centres/{centreId}/waivers` with a valid JWT
- **THEN** the system returns 403 Forbidden

#### Scenario: Unauthenticated request is rejected
- **WHEN** a request is made to `GET /api/centres/{centreId}/waivers` without a JWT
- **THEN** the system returns 401 Unauthorized
