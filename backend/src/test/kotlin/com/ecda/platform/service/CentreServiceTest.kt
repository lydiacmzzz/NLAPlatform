package com.ecda.platform.service

import com.ecda.platform.dto.CreateCentreRequest
import com.ecda.platform.dto.UpdateCentreRequest
import com.ecda.platform.model.*
import com.ecda.platform.repository.CentreLifecycleEventRepository
import com.ecda.platform.repository.CentreRepository
import com.ecda.platform.repository.KahDetailRepository
import com.ecda.platform.repository.WaiverHistoryRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.Optional

class CentreServiceTest {

    private val centreRepository: CentreRepository = mockk()
    private val kahDetailRepository: KahDetailRepository = mockk()
    private val lifecycleEventRepository: CentreLifecycleEventRepository = mockk()
    private val waiverHistoryRepository: WaiverHistoryRepository = mockk()
    private val centreScopeService: CentreScopeService = mockk()

    private val auth: Authentication = mockk()
    private lateinit var service: CentreService

    @BeforeEach
    fun setUp() {
        service = CentreService(centreRepository, kahDetailRepository, lifecycleEventRepository,
            waiverHistoryRepository, centreScopeService)
        every { centreScopeService.resolveScope(any()) } returns CentreScope.OfficerScope(listOf(1L))
        every { centreScopeService.toSpecification(any()) } returns Specification.where(null)
        justRun { centreScopeService.assertInScope(any(), any()) }
    }

    private fun sampleCentre(id: Long = 1L) = Centre(
        id = id,
        centreId = "CC-001",
        licenceNumber = "LIC-001",
        name = "Happy Kids Centre",
        centreType = CentreType.INFANT_CARE,
        address = "123 Test Street",
        postalCode = "123456",
        capacity = 50,
        licenceStatus = LicenceStatus.ACTIVE,
        licenceExpiryDate = LocalDate.now().plusYears(1),
        updatedBy = "officer1"
    )

    @Test
    fun `createCentre saves centre and records lifecycle event`() {
        val req = CreateCentreRequest(
            centreId = "CC-001", licenceNumber = "LIC-001", name = "Happy Kids",
            centreType = CentreType.INFANT_CARE, address = "123 Test St",
            postalCode = "123456", operatingHours = "7am-7pm", capacity = 50,
            licenceStatus = LicenceStatus.ACTIVE, licenceIssueDate = LocalDate.now(),
            licenceExpiryDate = LocalDate.now().plusYears(2),
            renewalDueDate = LocalDate.now().plusMonths(22)
        )
        val saved = sampleCentre()

        every { centreRepository.existsByCentreId("CC-001") } returns false
        every { centreRepository.existsByLicenceNumber("LIC-001") } returns false
        every { centreRepository.save(any()) } returns saved
        every { lifecycleEventRepository.save(any()) } returns mockk()
        every { kahDetailRepository.findByCentreIdAndIsCurrentTrue(1L) } returns null
        every { lifecycleEventRepository.findByCentreIdOrderByOccurredAtDesc(1L) } returns emptyList()

        val result = service.createCentre(req, "officer1")

        assertEquals("CC-001", result.centreId)
        verify(exactly = 1) { centreRepository.save(any()) }
        verify(exactly = 1) { lifecycleEventRepository.save(any()) }
    }

    @Test
    fun `createCentre throws CONFLICT when centreId already exists`() {
        val req = CreateCentreRequest(
            centreId = "CC-001", licenceNumber = "LIC-999", name = "Duplicate",
            centreType = CentreType.STUDENT_CARE, address = "Addr", postalCode = "654321",
            operatingHours = null, capacity = 30, licenceStatus = LicenceStatus.PENDING_RENEWAL,
            licenceIssueDate = null, licenceExpiryDate = null, renewalDueDate = null
        )
        every { centreRepository.existsByCentreId("CC-001") } returns true

        assertThrows<ResponseStatusException> { service.createCentre(req, "officer1") }
        verify(exactly = 0) { centreRepository.save(any()) }
    }

    @Test
    fun `getCentre throws NOT_FOUND for unknown id`() {
        every { centreRepository.findById(99L) } returns Optional.empty()

        assertThrows<ResponseStatusException> { service.getCentre(99L, auth) }
    }

    @Test
    fun `updateCentre records STATUS_CHANGED event when status differs`() {
        val centre = sampleCentre()
        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { centreRepository.save(any()) } returns centre
        every { lifecycleEventRepository.save(any()) } returns mockk()
        every { kahDetailRepository.findByCentreIdAndIsCurrentTrue(1L) } returns null
        every { lifecycleEventRepository.findByCentreIdOrderByOccurredAtDesc(1L) } returns emptyList()

        val req = UpdateCentreRequest(
            name = null, address = null, postalCode = null, operatingHours = null,
            capacity = null, licenceStatus = LicenceStatus.SUSPENDED,
            licenceIssueDate = null, licenceExpiryDate = null, renewalDueDate = null,
            applicationStage = null
        )
        service.updateCentre(1L, req, "officer1", auth)

        val eventSlot = slot<CentreLifecycleEvent>()
        verify { lifecycleEventRepository.save(capture(eventSlot)) }
        assertEquals("STATUS_CHANGED", eventSlot.captured.eventType)
    }

    @Test
    fun `searchCentres returns paged results`() {
        val centre = sampleCentre()
        val page = PageImpl(listOf(centre))
        every { centreRepository.findAll(any<org.springframework.data.jpa.domain.Specification<Centre>>(), any<Pageable>()) } returns page
        every { kahDetailRepository.findByCentreIdAndIsCurrentTrue(1L) } returns null

        val result = service.searchCentres(com.ecda.platform.dto.CentreSearchRequest(), auth)
        assertEquals(1, result.totalElements)
        assertEquals("CC-001", result.content[0].centreId)
    }
}
