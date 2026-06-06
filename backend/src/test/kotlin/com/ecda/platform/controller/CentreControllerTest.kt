package com.ecda.platform.controller

import com.ecda.platform.config.SecurityConfig
import com.ecda.platform.dto.*
import com.ecda.platform.model.CentreType
import com.ecda.platform.model.LicenceStatus
import com.ecda.platform.security.JwtUtil
import com.ecda.platform.service.CentreService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.OffsetDateTime

@WebMvcTest(CentreController::class)
@Import(SecurityConfig::class)
@ActiveProfiles("test")
class CentreControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @MockkBean lateinit var centreService: CentreService
    @MockkBean lateinit var jwtUtil: JwtUtil

    private fun sampleProfile(id: Long = 1L) = CentreProfileDto(
        id = id, centreId = "CC-001", licenceNumber = "LIC-001",
        name = "Happy Kids Centre", centreType = CentreType.INFANT_CARE,
        address = "123 Test St", postalCode = "123456",
        operatingHours = "7am-7pm", capacity = 50,
        licenceStatus = LicenceStatus.ACTIVE,
        licenceIssueDate = LocalDate.now(),
        licenceExpiryDate = LocalDate.now().plusYears(2),
        renewalDueDate = LocalDate.now().plusMonths(22),
        applicationStage = null, updatedAt = OffsetDateTime.now(), updatedBy = "officer1",
        currentKah = null, contacts = emptyList(), lifecycleEvents = emptyList()
    )

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET centres returns paged results`() {
        every { centreService.searchCentres(any()) } returns PagedResponse(
            content = listOf(
                CentreSummaryDto(1L, "CC-001", "LIC-001", "Happy Kids Centre",
                    CentreType.INFANT_CARE, "123456", LicenceStatus.ACTIVE,
                    LocalDate.now().plusYears(2), null, OffsetDateTime.now())
            ),
            totalElements = 1, totalPages = 1, page = 0, size = 20
        )

        mockMvc.get("/api/centres").andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].centreId") { value("CC-001") }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET centre by id returns profile`() {
        every { centreService.getCentre(1L) } returns sampleProfile()

        mockMvc.get("/api/centres/1").andExpect {
            status { isOk() }
            jsonPath("$.licenceNumber") { value("LIC-001") }
            jsonPath("$.licenceStatus") { value("ACTIVE") }
        }
    }

    @Test
    fun `GET centres returns 403 when unauthenticated`() {
        mockMvc.get("/api/centres").andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `POST centre creates and returns 201`() {
        val req = CreateCentreRequest(
            centreId = "CC-002", licenceNumber = "LIC-002",
            name = "Sunshine Centre", centreType = CentreType.STUDENT_CARE,
            address = "456 Happy Road", postalCode = "654321",
            operatingHours = "7am-7pm", capacity = 80,
            licenceStatus = LicenceStatus.ACTIVE,
            licenceIssueDate = LocalDate.now(),
            licenceExpiryDate = LocalDate.now().plusYears(2),
            renewalDueDate = LocalDate.now().plusMonths(22)
        )
        every { centreService.createCentre(any(), any()) } returns sampleProfile(2L)

        mockMvc.post("/api/centres") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    @WithMockUser(roles = ["CENTRE_LEADER"])
    fun `POST centre returns 403 for CENTRE_LEADER`() {
        val req = CreateCentreRequest(
            centreId = "CC-003", licenceNumber = "LIC-003",
            name = "Blocked Centre", centreType = CentreType.INFANT_CARE,
            address = "789 Test Ave", postalCode = "789012",
            operatingHours = null, capacity = 40,
            licenceStatus = LicenceStatus.PENDING_RENEWAL,
            licenceIssueDate = null, licenceExpiryDate = null, renewalDueDate = null
        )

        mockMvc.post("/api/centres") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `PUT centre updates and returns 200`() {
        val updateReq = UpdateCentreRequest(
            name = "Updated Name",
            address = null,
            postalCode = null,
            operatingHours = null,
            capacity = null,
            licenceStatus = null,
            licenceIssueDate = null,
            licenceExpiryDate = null,
            renewalDueDate = null,
            applicationStage = null
        )
        every { centreService.updateCentre(1L, updateReq, any()) } returns sampleProfile(1L)

        mockMvc.post("/api/centres/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateReq)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["CENTRE_LEADER"])
    fun `PUT centre returns 403 for CENTRE_LEADER`() {
        val updateReq = UpdateCentreRequest(
            name = "Blocked Update",
            address = null,
            postalCode = null,
            operatingHours = null,
            capacity = null,
            licenceStatus = null,
            licenceIssueDate = null,
            licenceExpiryDate = null,
            renewalDueDate = null,
            applicationStage = null
        )

        mockMvc.post("/api/centres/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateReq)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET centres with filters applies search params`() {
        every { centreService.searchCentres(any()) } returns PagedResponse(
            content = listOf(
                CentreSummaryDto(1L, "CC-001", "LIC-001", "Happy Kids Centre",
                    CentreType.INFANT_CARE, "123456", LicenceStatus.ACTIVE,
                    LocalDate.now().plusYears(2), null, OffsetDateTime.now())
            ),
            totalElements = 1, totalPages = 1, page = 0, size = 20
        )

        mockMvc.get("/api/centres?name=Happy&page=0&size=20").andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
        }

    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET centre with invalid id returns 404`() {
        every { centreService.getCentre(999L) } throws org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND,
            "Centre not found"
        )

        mockMvc.get("/api/centres/999").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `POST centre with invalid data returns 400`() {
        val invalidReq = CreateCentreRequest(
            centreId = "", // Empty centreId
            licenceNumber = "LIC-001",
            name = "Test",
            centreType = CentreType.INFANT_CARE,
            address = "Address",
            postalCode = "123456",
            operatingHours = null,
            capacity = -1, // Invalid capacity
            licenceStatus = LicenceStatus.ACTIVE,
            licenceIssueDate = null,
            licenceExpiryDate = null,
            renewalDueDate = null
        )

        mockMvc.post("/api/centres") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidReq)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser(roles = ["HQ_ADMIN"])
    fun `HQ_ADMIN can create centre`() {
        val req = CreateCentreRequest(
            centreId = "CC-004", licenceNumber = "LIC-004",
            name = "Admin Centre", centreType = CentreType.STUDENT_CARE,
            address = "Admin St", postalCode = "999999",
            operatingHours = "9am-5pm", capacity = 100,
            licenceStatus = LicenceStatus.ACTIVE,
            licenceIssueDate = LocalDate.now(),
            licenceExpiryDate = LocalDate.now().plusYears(3),
            renewalDueDate = LocalDate.now().plusMonths(35)
        )
        every { centreService.createCentre(any(), any()) } returns sampleProfile(3L)

        mockMvc.post("/api/centres") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET centres with pagination works correctly`() {
        every { centreService.searchCentres(any()) } returns PagedResponse(
            content = emptyList(),
            totalElements = 100,
            totalPages = 5,
            page = 1,
            size = 20
        )

        mockMvc.get("/api/centres?page=1&size=20").andExpect {
            status { isOk() }
            jsonPath("$.page") { value(1) }
            jsonPath("$.totalPages") { value(5) }
        }
    }
}
