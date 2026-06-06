package com.ecda.platform.dto

import com.ecda.platform.model.*
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.OffsetDateTime

data class CentreProfileDto(
    val id: Long,
    val centreId: String,
    val licenceNumber: String,
    val name: String,
    val centreType: CentreType,
    val address: String,
    val postalCode: String,
    val operatingHours: String?,
    val capacity: Int,
    val licenceStatus: LicenceStatus,
    val licenceIssueDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val renewalDueDate: LocalDate?,
    val applicationStage: String?,
    val updatedAt: OffsetDateTime,
    val updatedBy: String?,
    val currentKah: KahDetailDto?,
    val contacts: List<CentreContactDto>,
    val lifecycleEvents: List<LifecycleEventDto>
)

data class CentreSummaryDto(
    val id: Long,
    val centreId: String,
    val licenceNumber: String,
    val name: String,
    val centreType: CentreType,
    val postalCode: String,
    val licenceStatus: LicenceStatus,
    val licenceExpiryDate: LocalDate?,
    val renewalDueDate: LocalDate?,
    val updatedAt: OffsetDateTime
)

data class CreateCentreRequest(
    @field:NotBlank val centreId: String,
    @field:NotBlank val licenceNumber: String,
    @field:NotBlank val name: String,
    @field:NotNull val centreType: CentreType,
    @field:NotBlank val address: String,
    @field:NotBlank @field:Size(min = 6, max = 6) val postalCode: String,
    val operatingHours: String?,
    @field:Positive val capacity: Int,
    val licenceStatus: LicenceStatus = LicenceStatus.PENDING_RENEWAL,
    val licenceIssueDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val renewalDueDate: LocalDate?
)

data class UpdateCentreRequest(
    val name: String?,
    val address: String?,
    val postalCode: String?,
    val operatingHours: String?,
    val capacity: Int?,
    val licenceStatus: LicenceStatus?,
    val licenceIssueDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val renewalDueDate: LocalDate?,
    val applicationStage: String?
)

data class KahDetailDto(
    val id: Long,
    val principalName: String,
    val nric: String,
    val email: String?,
    val phone: String?,
    val licenceConditions: String?,
    val appointmentStartDate: LocalDate,
    val appointmentEndDate: LocalDate?,
    val isCurrent: Boolean,
    val pendingApproval: Boolean
)

data class CreateKahRequest(
    @field:NotBlank val principalName: String,
    @field:NotBlank val nric: String,
    val email: String?,
    val phone: String?,
    val licenceConditions: String?,
    @field:NotNull val appointmentStartDate: LocalDate
)

data class UpdateKahRequest(
    val email: String?,
    val phone: String?,
    val licenceConditions: String?,
    val pendingApproval: Boolean?
)

data class CentreContactDto(
    val id: Long,
    val contactType: ContactType,
    val contactName: String,
    val role: String?,
    val email: String?,
    val phone: String?
)

data class UpsertContactRequest(
    @field:NotNull val contactType: ContactType,
    @field:NotBlank val contactName: String,
    val role: String?,
    val email: String?,
    val phone: String?
)

data class LifecycleEventDto(
    val id: Long,
    val eventType: String,
    val description: String,
    val occurredAt: OffsetDateTime,
    val recordedBy: String
)

data class CentreSearchRequest(
    val query: String? = null,
    val centreType: CentreType? = null,
    val licenceStatus: LicenceStatus? = null,
    val renewalDueBefore: LocalDate? = null,
    val sortBy: String = "updatedAt",
    val sortDir: String = "desc",
    val page: Int = 0,
    val size: Int = 20
)

data class PagedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

data class WaiverHistoryDto(
    val id: Long,
    val waiverType: String,
    val waiverTitle: String,
    val waiverDescription: String?,
    val waiverStatus: WaiverStatus,
    val approvalDate: LocalDate?,
    val expiryDate: LocalDate?,
    val approvedBy: String?,
    val officerRemarks: String?,
    val supportingDocumentName: String?,
    val supportingDocumentUrl: String?
)
