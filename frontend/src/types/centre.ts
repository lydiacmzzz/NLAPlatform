export type CentreType = 'INFANT_CARE' | 'STUDENT_CARE' | 'ANCHOR_OPERATOR' | 'PARTNER_OPERATOR'
export type LicenceStatus = 'ACTIVE' | 'PENDING_RENEWAL' | 'SUSPENDED' | 'EXPIRED'
export type ContactType = 'PRIMARY' | 'HQ_LIAISON' | 'EMERGENCY'
export type UserRole = 'ECDA_OFFICER' | 'HQ_ADMIN' | 'CENTRE_LEADER'

export interface KahDetailDto {
  id: number
  principalName: string
  nric: string
  email: string | null
  phone: string | null
  licenceConditions: string | null
  appointmentStartDate: string
  appointmentEndDate: string | null
  isCurrent: boolean
  pendingApproval: boolean
}

export interface CentreContactDto {
  id: number
  contactType: ContactType
  contactName: string
  role: string | null
  email: string | null
  phone: string | null
}

export interface LifecycleEventDto {
  id: number
  eventType: string
  description: string
  occurredAt: string
  recordedBy: string
}

export interface CentreProfileDto {
  id: number
  centreId: string
  licenceNumber: string
  name: string
  centreType: CentreType
  address: string
  postalCode: string
  operatingHours: string | null
  capacity: number
  licenceStatus: LicenceStatus
  licenceIssueDate: string | null
  licenceExpiryDate: string | null
  renewalDueDate: string | null
  applicationStage: string | null
  updatedAt: string
  updatedBy: string | null
  currentKah: KahDetailDto | null
  contacts: CentreContactDto[]
  lifecycleEvents: LifecycleEventDto[]
}

export interface CentreSummaryDto {
  id: number
  centreId: string
  licenceNumber: string
  name: string
  centreType: CentreType
  postalCode: string
  licenceStatus: LicenceStatus
  licenceExpiryDate: string | null
  renewalDueDate: string | null
  updatedAt: string
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface CentreSearchParams {
  query?: string
  centreType?: CentreType
  licenceStatus?: LicenceStatus
  renewalDueBefore?: string
  sortBy?: 'licenceExpiryDate' | 'name' | 'updatedAt'
  sortDir?: 'asc' | 'desc'
  page?: number
  size?: number
}

export interface UpdateCentreRequest {
  name?: string
  address?: string
  postalCode?: string
  operatingHours?: string
  capacity?: number
  licenceStatus?: LicenceStatus
  licenceIssueDate?: string
  licenceExpiryDate?: string
  renewalDueDate?: string
  applicationStage?: string
}

export interface CreateKahRequest {
  principalName: string
  nric: string
  email?: string
  phone?: string
  licenceConditions?: string
  appointmentStartDate: string
}

export interface AuthUser {
  username: string
  role: UserRole
  fullName: string | null
  token: string
}

export type WaiverStatus = 'APPROVED' | 'EXPIRED' | 'SUPERSEDED' | 'REJECTED'

export interface WaiverHistoryDto {
  id: number
  waiverType: string
  waiverTitle: string
  waiverDescription: string | null
  waiverStatus: WaiverStatus
  approvalDate: string | null
  expiryDate: string | null
  approvedBy: string | null
  officerRemarks: string | null
  supportingDocumentName: string | null
  supportingDocumentUrl: string | null
}
