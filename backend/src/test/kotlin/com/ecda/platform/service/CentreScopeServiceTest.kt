package com.ecda.platform.service

import com.ecda.platform.model.*
import com.ecda.platform.repository.CentreRepository
import com.ecda.platform.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class CentreScopeServiceTest {

    private val userRepository: UserRepository = mockk()
    private val centreRepository: CentreRepository = mockk()
    private lateinit var service: CentreScopeService

    @BeforeEach
    fun setUp() {
        service = CentreScopeService(userRepository, centreRepository)
    }

    private fun auth(username: String): Authentication = mockk<Authentication>().also {
        every { it.name } returns username
    }

    private fun user(role: UserRole, hqId: Long? = null, centreId: Long? = null, id: Long = 1L) =
        User(id = id, username = "u", email = "u@e.sg", passwordHash = "h", role = role,
            hqId = hqId, centreId = centreId)

    private fun centre(id: Long = 1L, hqId: Long = 10L) =
        Centre(id = id, centreId = "CC-001", licenceNumber = "L", name = "C",
            centreType = CentreType.INFANT_CARE, address = "A", postalCode = "123456",
            capacity = 50, licenceStatus = LicenceStatus.ACTIVE, hq = Hq(id = hqId, code = "HQ-A", name = "HQ A"))

    // ── resolveScope ──────────────────────────────────────────────────

    @Test
    fun `resolveScope returns OfficerScope with assigned HQ ids for ECDA_OFFICER`() {
        every { userRepository.findByUsername("officer1") } returns user(UserRole.ECDA_OFFICER, id = 5L)
        every { userRepository.findAssignedHqIds(5L) } returns listOf(10L, 20L)

        val scope = service.resolveScope(auth("officer1"))

        assertTrue(scope is CentreScope.OfficerScope)
        assertEquals(listOf(10L, 20L), (scope as CentreScope.OfficerScope).hqIds)
    }

    @Test
    fun `resolveScope returns AdminScope with hqId for HQ_ADMIN`() {
        every { userRepository.findByUsername("HQAAdmin") } returns user(UserRole.HQ_ADMIN, hqId = 10L)

        val scope = service.resolveScope(auth("HQAAdmin"))

        assertTrue(scope is CentreScope.AdminScope)
        assertEquals(10L, (scope as CentreScope.AdminScope).hqId)
    }

    @Test
    fun `resolveScope throws FORBIDDEN for HQ_ADMIN with no hqId`() {
        every { userRepository.findByUsername("HQAAdmin") } returns user(UserRole.HQ_ADMIN, hqId = null)

        val ex = assertThrows<ResponseStatusException> { service.resolveScope(auth("HQAAdmin")) }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `resolveScope returns LeaderScope with centreId for CENTRE_LEADER`() {
        every { userRepository.findByUsername("HQACenterLeader1") } returns user(UserRole.CENTRE_LEADER, centreId = 1L)

        val scope = service.resolveScope(auth("HQACenterLeader1"))

        assertTrue(scope is CentreScope.LeaderScope)
        assertEquals(1L, (scope as CentreScope.LeaderScope).centreId)
    }

    @Test
    fun `resolveScope throws FORBIDDEN for CENTRE_LEADER with no centreId`() {
        every { userRepository.findByUsername("HQACenterLeader1") } returns user(UserRole.CENTRE_LEADER, centreId = null)

        val ex = assertThrows<ResponseStatusException> { service.resolveScope(auth("HQACenterLeader1")) }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    // ── assertInScope ─────────────────────────────────────────────────

    @Test
    fun `assertInScope passes for OfficerScope when centre hq is in assigned list`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L, hqId = 10L))

        assertDoesNotThrow { service.assertInScope(1L, CentreScope.OfficerScope(listOf(10L, 20L))) }
    }

    @Test
    fun `assertInScope throws FORBIDDEN for OfficerScope when centre hq is not assigned`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L, hqId = 30L))

        val ex = assertThrows<ResponseStatusException> {
            service.assertInScope(1L, CentreScope.OfficerScope(listOf(10L, 20L)))
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `assertInScope passes for AdminScope when centre hq matches`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L, hqId = 10L))

        assertDoesNotThrow { service.assertInScope(1L, CentreScope.AdminScope(10L)) }
    }

    @Test
    fun `assertInScope throws FORBIDDEN for AdminScope when centre hq differs`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L, hqId = 20L))

        val ex = assertThrows<ResponseStatusException> {
            service.assertInScope(1L, CentreScope.AdminScope(10L))
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `assertInScope passes for LeaderScope when centreId matches`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L))

        assertDoesNotThrow { service.assertInScope(1L, CentreScope.LeaderScope(1L)) }
    }

    @Test
    fun `assertInScope throws FORBIDDEN for LeaderScope when centreId differs`() {
        every { centreRepository.findById(2L) } returns Optional.of(centre(id = 2L))

        val ex = assertThrows<ResponseStatusException> {
            service.assertInScope(2L, CentreScope.LeaderScope(1L))
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `assertInScope throws NOT_FOUND when centre does not exist`() {
        every { centreRepository.findById(99L) } returns Optional.empty()

        val ex = assertThrows<ResponseStatusException> {
            service.assertInScope(99L, CentreScope.OfficerScope(listOf(10L)))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `assertInScope throws FORBIDDEN for OfficerScope with empty hqIds list`() {
        every { centreRepository.findById(1L) } returns Optional.of(centre(id = 1L, hqId = 10L))

        val ex = assertThrows<ResponseStatusException> {
            service.assertInScope(1L, CentreScope.OfficerScope(emptyList()))
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }
}
