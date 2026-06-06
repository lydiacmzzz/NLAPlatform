package com.ecda.platform.controller

import com.ecda.platform.config.SecurityConfig
import com.ecda.platform.dto.WaiverHistoryDto
import com.ecda.platform.model.WaiverStatus
import com.ecda.platform.security.JwtUtil
import com.ecda.platform.service.CentreService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@WebMvcTest(CentreController::class)
@Import(SecurityConfig::class)
@ActiveProfiles("test")
class WaiverHistoryControllerTest {

    @Autowired lateinit var mockMvc: MockMvc

    @MockkBean lateinit var centreService: CentreService
    @MockkBean lateinit var jwtUtil: JwtUtil

    private fun sampleWaiver(id: Long = 1L) = WaiverHistoryDto(
        id = id,
        waiverType = "Physical Environment",
        waiverTitle = "Outdoor Play Area Waiver",
        waiverDescription = "Waiver for outdoor play area requirement",
        waiverStatus = WaiverStatus.APPROVED,
        approvalDate = LocalDate.of(2023, 3, 15),
        expiryDate = LocalDate.of(2025, 3, 14),
        approvedBy = "officer1",
        officerRemarks = "Approved with conditions",
        supportingDocumentName = "waiver.pdf",
        supportingDocumentUrl = "https://docs.ecda.gov.sg/waivers/waiver.pdf"
    )

    // ── Officer access ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET waivers returns 200 for ECDA_OFFICER`() {
        every { centreService.getWaiverHistory(1L) } returns listOf(sampleWaiver())

        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isOk() }
            jsonPath("$[0].waiverTitle") { value("Outdoor Play Area Waiver") }
            jsonPath("$[0].waiverStatus") { value("APPROVED") }
        }
    }

    @Test
    @WithMockUser(roles = ["HQ_ADMIN"])
    fun `GET waivers returns 200 for HQ_ADMIN`() {
        every { centreService.getWaiverHistory(1L) } returns listOf(sampleWaiver())

        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isOk() }
        }
    }

    // ── Role restrictions ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = ["CENTRE_LEADER"])
    fun `GET waivers returns 403 for CENTRE_LEADER`() {
        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `GET waivers returns 403 for unauthenticated user`() {
        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isForbidden() }
        }
    }

    // ── Response shape ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET waivers returns empty list when no waivers exist`() {
        every { centreService.getWaiverHistory(1L) } returns emptyList()

        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET waivers includes supporting document fields`() {
        every { centreService.getWaiverHistory(1L) } returns listOf(sampleWaiver())

        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isOk() }
            jsonPath("$[0].supportingDocumentName") { value("waiver.pdf") }
            jsonPath("$[0].supportingDocumentUrl") { value("https://docs.ecda.gov.sg/waivers/waiver.pdf") }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET waivers returns 404 when centre does not exist`() {
        every { centreService.getWaiverHistory(999L) } throws
            org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Centre not found"
            )

        mockMvc.get("/api/centres/999/waivers").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ECDA_OFFICER"])
    fun `GET waivers includes officer remarks`() {
        every { centreService.getWaiverHistory(1L) } returns listOf(sampleWaiver())

        mockMvc.get("/api/centres/1/waivers").andExpect {
            status { isOk() }
            jsonPath("$[0].officerRemarks") { value("Approved with conditions") }
            jsonPath("$[0].approvedBy") { value("officer1") }
        }
    }
}
