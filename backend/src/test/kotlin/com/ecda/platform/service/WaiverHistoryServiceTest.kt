package com.ecda.platform.service

import com.ecda.platform.model.*
import com.ecda.platform.repository.CentreRepository
import com.ecda.platform.repository.KahDetailRepository
import com.ecda.platform.repository.CentreLifecycleEventRepository
import com.ecda.platform.repository.WaiverHistoryRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.Optional

class WaiverHistoryServiceTest {

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
        licenceStatus = LicenceStatus.ACTIVE
    )

    private fun sampleWaiver(centre: Centre, id: Long = 1L) = WaiverHistory(
        id = id,
        centre = centre,
        waiverType = "Physical Environment",
        waiverTitle = "Outdoor Play Area Waiver",
        waiverDescription = "Waiver for outdoor play area requirement due to space constraints",
        waiverStatus = WaiverStatus.APPROVED,
        approvalDate = LocalDate.of(2023, 3, 15),
        expiryDate = LocalDate.of(2025, 3, 14),
        approvedBy = "officer1",
        officerRemarks = "Approved with condition to provide indoor alternatives",
        supportingDocumentName = "outdoor_waiver_approval.pdf",
        supportingDocumentUrl = "https://docs.ecda.gov.sg/waivers/outdoor_waiver_approval.pdf"
    )

    @Test
    fun `getWaiverHistory returns list of waiver DTOs for valid centre`() {
        val centre = sampleCentre()
        val waiver = sampleWaiver(centre)

        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(1L) } returns listOf(waiver)

        val result = service.getWaiverHistory(1L, auth)

        assertEquals(1, result.size)
        assertEquals("Outdoor Play Area Waiver", result[0].waiverTitle)
        assertEquals("Physical Environment", result[0].waiverType)
        assertEquals(WaiverStatus.APPROVED, result[0].waiverStatus)
        assertEquals("officer1", result[0].approvedBy)
        assertEquals("outdoor_waiver_approval.pdf", result[0].supportingDocumentName)
    }

    @Test
    fun `getWaiverHistory returns empty list when no waivers exist`() {
        val centre = sampleCentre()

        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(1L) } returns emptyList()

        val result = service.getWaiverHistory(1L, auth)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getWaiverHistory throws NOT_FOUND for unknown centre`() {
        every { centreRepository.findById(99L) } returns Optional.empty()

        assertThrows<ResponseStatusException> { service.getWaiverHistory(99L, auth) }
        verify(exactly = 0) { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(any()) }
    }

    @Test
    fun `getWaiverHistory maps all DTO fields correctly`() {
        val centre = sampleCentre()
        val waiver = sampleWaiver(centre)

        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(1L) } returns listOf(waiver)

        val result = service.getWaiverHistory(1L, auth)
        val dto = result[0]

        assertEquals(1L, dto.id)
        assertEquals("Physical Environment", dto.waiverType)
        assertEquals("Outdoor Play Area Waiver", dto.waiverTitle)
        assertEquals("Waiver for outdoor play area requirement due to space constraints", dto.waiverDescription)
        assertEquals(WaiverStatus.APPROVED, dto.waiverStatus)
        assertEquals(LocalDate.of(2023, 3, 15), dto.approvalDate)
        assertEquals(LocalDate.of(2025, 3, 14), dto.expiryDate)
        assertEquals("Approved with condition to provide indoor alternatives", dto.officerRemarks)
        assertEquals("https://docs.ecda.gov.sg/waivers/outdoor_waiver_approval.pdf", dto.supportingDocumentUrl)
    }

    @Test
    fun `getWaiverHistory handles waivers with null optional fields`() {
        val centre = sampleCentre()
        val waiver = WaiverHistory(
            id = 2L,
            centre = centre,
            waiverType = "Staffing",
            waiverTitle = "Temporary Staff Deployment",
            waiverStatus = WaiverStatus.REJECTED
        )

        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(1L) } returns listOf(waiver)

        val result = service.getWaiverHistory(1L, auth)

        assertEquals(1, result.size)
        assertNull(result[0].approvalDate)
        assertNull(result[0].expiryDate)
        assertNull(result[0].approvedBy)
        assertNull(result[0].officerRemarks)
        assertNull(result[0].supportingDocumentName)
        assertNull(result[0].supportingDocumentUrl)
    }

    @Test
    fun `getWaiverHistory returns multiple waivers across different statuses`() {
        val centre = sampleCentre()
        val waivers = listOf(
            sampleWaiver(centre, 1L),
            WaiverHistory(id = 2L, centre = centre, waiverType = "Capacity", waiverTitle = "Capacity Exception",
                waiverStatus = WaiverStatus.EXPIRED, approvalDate = LocalDate.of(2021, 1, 1), expiryDate = LocalDate.of(2022, 1, 1)),
            WaiverHistory(id = 3L, centre = centre, waiverType = "Renovation", waiverTitle = "Post-Renovation Waiver",
                waiverStatus = WaiverStatus.SUPERSEDED)
        )

        every { centreRepository.findById(1L) } returns Optional.of(centre)
        every { waiverHistoryRepository.findByCentreIdOrderByApprovalDateDesc(1L) } returns waivers

        val result = service.getWaiverHistory(1L, auth)

        assertEquals(3, result.size)
        val statuses = result.map { it.waiverStatus }
        assertTrue(statuses.contains(WaiverStatus.APPROVED))
        assertTrue(statuses.contains(WaiverStatus.EXPIRED))
        assertTrue(statuses.contains(WaiverStatus.SUPERSEDED))
    }
}
