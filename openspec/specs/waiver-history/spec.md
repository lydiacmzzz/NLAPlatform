# Waiver History

## Purpose

This capability covers the read-only Waiver History feature for the Centre Detail page. It defines the API endpoint that exposes waiver records for a given centre and the frontend rules that control visibility and rendering of the Waiver History section.

## Requirements

### Requirement: Waiver history is accessible only to ECDA Officers
The system SHALL restrict access to `GET /api/centres/{centreId}/waivers` to users with the `ECDA_OFFICER` role. All other roles (including `HQ_ADMIN` and `CENTRE_LEADER`) SHALL receive a 403 Forbidden response. Unauthenticated requests SHALL receive 401 Unauthorized.

#### Scenario: ECDA Officer retrieves waiver history
- **WHEN** an `ECDA_OFFICER` sends `GET /api/centres/{centreId}/waivers` with a valid JWT
- **THEN** the system returns 200 with a JSON array of waiver records for that centre

#### Scenario: HQ Admin is denied access
- **WHEN** an `HQ_ADMIN` sends `GET /api/centres/{centreId}/waivers` with a valid JWT
- **THEN** the system returns 403 Forbidden and the response body does not contain any waiver field names

#### Scenario: Centre Leader is denied access
- **WHEN** a `CENTRE_LEADER` sends `GET /api/centres/{centreId}/waivers` with a valid JWT
- **THEN** the system returns 403 Forbidden

#### Scenario: Unauthenticated request is rejected
- **WHEN** a request is made to `GET /api/centres/{centreId}/waivers` without a JWT
- **THEN** the system returns 401 Unauthorized

### Requirement: Waiver history endpoint returns 404 for unknown centres
The system SHALL return 404 Not Found when the requested `centreId` does not exist in the database.

#### Scenario: Centre not found
- **WHEN** an `ECDA_OFFICER` requests waivers for a `centreId` that does not exist
- **THEN** the system returns 404 Not Found

### Requirement: Waiver records include all required fields
Each waiver record returned by the API SHALL include: `id`, `waiverType`, `waiverTitle`, `waiverStatus`, and optionally `waiverDescription`, `approvalDate`, `expiryDate`, `approvedBy`, `officerRemarks`, `supportingDocumentName`, `supportingDocumentUrl`.

#### Scenario: Full waiver record is returned
- **WHEN** an `ECDA_OFFICER` retrieves waivers for a centre with an APPROVED waiver
- **THEN** the response includes `waiverTitle`, `waiverType`, `waiverStatus`, `approvalDate`, `expiryDate`, `approvedBy`, `officerRemarks`, `supportingDocumentName`, and `supportingDocumentUrl`

#### Scenario: Waiver with null optional fields is returned
- **WHEN** an `ECDA_OFFICER` retrieves a waiver with no approval date or supporting document
- **THEN** the optional fields are `null` in the response (not omitted)

#### Scenario: Empty list when no waivers exist
- **WHEN** an `ECDA_OFFICER` retrieves waivers for a centre with no waiver records
- **THEN** the system returns 200 with an empty JSON array

### Requirement: Waiver status values are constrained
The `waiverStatus` field SHALL be one of: `APPROVED`, `EXPIRED`, `SUPERSEDED`, `REJECTED`.

#### Scenario: All status values are handled
- **WHEN** waivers with each of the four statuses exist for a centre
- **THEN** all four are returned with the correct status string

### Requirement: Frontend shows waiver history only to ECDA Officers
The frontend SHALL display the Waiver History section on the Centre Detail page only when the authenticated user has the `ECDA_OFFICER` role. The section SHALL be hidden for `HQ_ADMIN` and `CENTRE_LEADER` users.

#### Scenario: ECDA Officer sees Waiver History section
- **WHEN** an `ECDA_OFFICER` views a centre's detail page
- **THEN** the Waiver History section is rendered

#### Scenario: HQ Admin does not see Waiver History section
- **WHEN** an `HQ_ADMIN` views a centre's detail page
- **THEN** the Waiver History section is not rendered

### Requirement: Waiver history is read-only
The Waiver History section SHALL not expose any add, edit, or delete controls. Users SHALL not be able to modify waiver records through the UI.

#### Scenario: No mutation controls are rendered
- **WHEN** an `ECDA_OFFICER` views the Waiver History section
- **THEN** no buttons labelled "Add", "Edit", or "Delete" are present in the section

### Requirement: Waiver records are sorted by approval date descending
The API SHALL return waiver records ordered by `approval_date` descending (most recent first). Records with null `approval_date` appear last.

#### Scenario: Waivers sorted newest first
- **WHEN** a centre has multiple waivers with different approval dates
- **THEN** the record with the most recent `approval_date` appears first in the response
