package com.ecda.platform.service

import com.ecda.platform.dto.*
import com.ecda.platform.model.*
import com.ecda.platform.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
@Transactional
class CentreService(
    private val centreRepository: CentreRepository,
    private val kahDetailRepository: KahDetailRepository,
    private val lifecycleEventRepository: CentreLifecycleEventRepository,
    private val waiverHistoryRepository: WaiverHistoryRepository
) {

    @Transactional(readOnly = true)
    fun searchCentres(req: CentreSearchRequest): PagedResponse<CentreSummaryDto> {
        val sort = buildSort(req.sortBy, req.sortDir)
        val pageable = PageRequest.of(req.page, req.size, sort)
        val page = centreRepository.findAll(buildSpec(req), pageable)
        return PagedResponse(
            content = page.content.map { it.toSummaryDto() },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            size = page.size
        )
    }

    private fun buildSpec(req: CentreSearchRequest): Specification<Centre> = Specification { root, _, cb ->
        val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()
        req.query?.let { q ->
            val pattern = "%${q.lowercase()}%"
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("licenceNumber")), pattern),
                cb.like(cb.lower(root.get("postalCode")), pattern)
            ))
        }
        req.centreType?.let { predicates.add(cb.equal(root.get<CentreType>("centreType"), it)) }
        req.licenceStatus?.let { predicates.add(cb.equal(root.get<LicenceStatus>("licenceStatus"), it)) }
        req.renewalDueBefore?.let { predicates.add(cb.lessThanOrEqualTo(root.get("renewalDueDate"), it)) }
        cb.and(*predicates.toTypedArray())
    }

    @Transactional(readOnly = true)
    fun getCentre(id: Long): CentreProfileDto = findCentreOrThrow(id).toProfileDto()

    @Transactional(readOnly = true)
    fun getCentreByCentreId(centreId: String): CentreProfileDto =
        (centreRepository.findByCentreId(centreId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Centre not found")).toProfileDto()

    fun createCentre(req: CreateCentreRequest, actor: String): CentreProfileDto {
        if (centreRepository.existsByCentreId(req.centreId))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Centre ID already exists")
        if (centreRepository.existsByLicenceNumber(req.licenceNumber))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Licence number already exists")

        val centre = centreRepository.save(
            Centre(
                centreId = req.centreId,
                licenceNumber = req.licenceNumber,
                name = req.name,
                centreType = req.centreType,
                address = req.address,
                postalCode = req.postalCode,
                operatingHours = req.operatingHours,
                capacity = req.capacity,
                licenceStatus = req.licenceStatus,
                licenceIssueDate = req.licenceIssueDate,
                licenceExpiryDate = req.licenceExpiryDate,
                renewalDueDate = req.renewalDueDate,
                updatedBy = actor
            )
        )
        recordEvent(centre, "CENTRE_REGISTERED", "Centre registered in the system", actor)
        return centre.toProfileDto()
    }

    fun updateCentre(id: Long, req: UpdateCentreRequest, actor: String): CentreProfileDto {
        val centre = findCentreOrThrow(id)
        val oldStatus = centre.licenceStatus

        req.name?.let { centre.name = it }
        req.address?.let { centre.address = it }
        req.postalCode?.let { centre.postalCode = it }
        req.operatingHours?.let { centre.operatingHours = it }
        req.capacity?.let { centre.capacity = it }
        req.licenceStatus?.let { centre.licenceStatus = it }
        req.licenceIssueDate?.let { centre.licenceIssueDate = it }
        req.licenceExpiryDate?.let { centre.licenceExpiryDate = it }
        req.renewalDueDate?.let { centre.renewalDueDate = it }
        req.applicationStage?.let { centre.applicationStage = it }
        centre.updatedAt = OffsetDateTime.now()
        centre.updatedBy = actor

        if (req.licenceStatus != null && req.licenceStatus != oldStatus) {
            recordEvent(centre, "STATUS_CHANGED", "Licence status changed from $oldStatus to ${req.licenceStatus}", actor)
        } else {
            recordEvent(centre, "PROFILE_UPDATED", "Centre profile updated", actor)
        }

        return centreRepository.save(centre).toProfileDto()
    }

    fun addKah(centreId: Long, req: CreateKahRequest, actor: String): KahDetailDto {
        val centre = findCentreOrThrow(centreId)
        kahDetailRepository.findByCentreIdAndIsCurrentTrue(centreId)?.let { existing ->
            existing.isCurrent = false
            existing.appointmentEndDate = req.appointmentStartDate.minusDays(1)
            kahDetailRepository.save(existing)
        }
        val kah = kahDetailRepository.save(
            KahDetail(
                centre = centre,
                principalName = req.principalName,
                nric = req.nric,
                email = req.email,
                phone = req.phone,
                licenceConditions = req.licenceConditions,
                appointmentStartDate = req.appointmentStartDate
            )
        )
        recordEvent(centre, "KAH_APPOINTED", "New KAH appointed: ${req.principalName}", actor)
        centre.updatedAt = OffsetDateTime.now()
        centre.updatedBy = actor
        centreRepository.save(centre)
        return kah.toDto()
    }

    fun updateKah(centreId: Long, kahId: Long, req: UpdateKahRequest, actor: String): KahDetailDto {
        val kah = kahDetailRepository.findById(kahId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "KAH record not found")
        }
        if (kah.centre.id != centreId) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "KAH does not belong to this centre")

        req.email?.let { kah.email = it }
        req.phone?.let { kah.phone = it }
        req.licenceConditions?.let { kah.licenceConditions = it }
        req.pendingApproval?.let { kah.pendingApproval = it }

        if (req.pendingApproval == true) {
            recordEvent(findCentreOrThrow(centreId), "KAH_CHANGE_PENDING", "KAH change flagged pending officer approval", actor)
        }

        return kahDetailRepository.save(kah).toDto()
    }

    @Transactional(readOnly = true)
    fun getKahHistory(centreId: Long): List<KahDetailDto> {
        findCentreOrThrow(centreId)
        return kahDetailRepository.findByCentreIdOrderByCreatedAtDesc(centreId).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getWaiverHistory(centreId: Long): List<WaiverHistoryDto> {
        findCentreOrThrow(centreId)
        return waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(centreId).map { it.toDto() }
    }

    private fun findCentreOrThrow(id: Long): Centre =
        centreRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Centre not found") }

    private fun recordEvent(centre: Centre, eventType: String, description: String, actor: String) {
        lifecycleEventRepository.save(
            CentreLifecycleEvent(centre = centre, eventType = eventType, description = description, recordedBy = actor)
        )
    }

    private fun buildSort(sortBy: String, sortDir: String): Sort {
        val direction = if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val field = when (sortBy) {
            "licenceExpiryDate" -> "licenceExpiryDate"
            "name" -> "name"
            else -> "updatedAt"
        }
        return Sort.by(direction, field)
    }

    private fun Centre.toSummaryDto() = CentreSummaryDto(
        id, centreId, licenceNumber, name, centreType, postalCode,
        licenceStatus, licenceExpiryDate, renewalDueDate, updatedAt
    )

    private fun Centre.toProfileDto(): CentreProfileDto {
        val currentKah = kahDetailRepository.findByCentreIdAndIsCurrentTrue(id)
        val contacts = this.contacts.map { it.toDto() }
        val events = lifecycleEventRepository.findByCentreIdOrderByOccurredAtDesc(id).map { it.toDto() }
        return CentreProfileDto(
            id, centreId, licenceNumber, name, centreType, address, postalCode,
            operatingHours, capacity, licenceStatus, licenceIssueDate, licenceExpiryDate,
            renewalDueDate, applicationStage, updatedAt, updatedBy,
            currentKah?.toDto(), contacts, events
        )
    }

    private fun KahDetail.toDto() = KahDetailDto(
        id, principalName, nric, email, phone, licenceConditions,
        appointmentStartDate, appointmentEndDate, isCurrent, pendingApproval
    )

    private fun CentreContact.toDto() = CentreContactDto(id, contactType, contactName, role, email, phone)

    private fun CentreLifecycleEvent.toDto() = LifecycleEventDto(id, eventType, description, occurredAt, recordedBy)

    private fun WaiverHistory.toDto() = WaiverHistoryDto(
        id, waiverType, waiverTitle, waiverDescription, waiverStatus,
        approvalDate, expiryDate, approvedBy, officerRemarks,
        supportingDocumentName, supportingDocumentUrl
    )
}
