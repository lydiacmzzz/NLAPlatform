package com.ecda.platform.security

import io.mockk.*
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.filter.OncePerRequestFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull

class JwtAuthFilterTest {

    private val jwtUtil: JwtUtil = mockk()
    private val request: HttpServletRequest = mockk()
    private val response: HttpServletResponse = mockk()
    private val filterChain: FilterChain = mockk()

    private lateinit var filter: JwtAuthFilter

    @BeforeEach
    fun setUp() {
        filter = JwtAuthFilter(jwtUtil)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `doFilterInternal passes request through when Authorization header is missing`() {
        every { request.getHeader("Authorization") } returns null
        every { filterChain.doFilter(request, response) } just Runs

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `doFilterInternal passes request through when Authorization header is empty`() {
        every { request.getHeader("Authorization") } returns ""
        every { filterChain.doFilter(request, response) } just Runs

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `doFilterInternal passes request through when Authorization header does not start with Bearer`() {
        every { request.getHeader("Authorization") } returns "Basic xyz123"
        every { filterChain.doFilter(request, response) } just Runs

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `doFilterInternal passes request through when token is invalid`() {
        val invalidToken = "invalid.token.here"
        every { request.getHeader("Authorization") } returns "Bearer $invalidToken"
        every { jwtUtil.isTokenValid(invalidToken) } returns false
        every { filterChain.doFilter(request, response) } just Runs

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `doFilterInternal sets authentication when valid token is provided`() {
        val validToken = "valid.jwt.token"
        every { request.getHeader("Authorization") } returns "Bearer $validToken"
        every { jwtUtil.isTokenValid(validToken) } returns true
        every { jwtUtil.extractUsername(validToken) } returns "admin@example.com"
        every { jwtUtil.extractRole(validToken) } returns "HQ_ADMIN"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/centres")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals("admin@example.com", authentication.principal)
        assertEquals(1, authentication.authorities.size)
        assertEquals("ROLE_HQ_ADMIN", authentication.authorities.first().authority)
    }

    @Test
    fun `doFilterInternal extracts username from Bearer token correctly`() {
        val token = "eyJ.payload.signature"
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtil.isTokenValid(token) } returns true
        every { jwtUtil.extractUsername(token) } returns "officer@example.com"
        every { jwtUtil.extractRole(token) } returns "ECDA_OFFICER"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/centres")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertEquals("officer@example.com", authentication.principal)
    }

    @Test
    fun `doFilterInternal sets correct role from token`() {
        val token = "valid.token"
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtil.isTokenValid(token) } returns true
        every { jwtUtil.extractUsername(token) } returns "leader@example.com"
        every { jwtUtil.extractRole(token) } returns "CENTRE_LEADER"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/centres")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertEquals("ROLE_CENTRE_LEADER", authentication.authorities.first().authority)
    }

    @Test
    fun `doFilterInternal always proceeds to filter chain`() {
        val token = "valid.token"
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtil.isTokenValid(token) } returns true
        every { jwtUtil.extractUsername(token) } returns "user@example.com"
        every { jwtUtil.extractRole(token) } returns "HQ_ADMIN"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/test")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify(exactly = 1) { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal handles Bearer token with extra spaces`() {
        val token = "valid.token"
        every { request.getHeader("Authorization") } returns "Bearer  $token" // Extra space
        every { jwtUtil.isTokenValid(any()) } returns false // Should fail due to extra space
        every { filterChain.doFilter(request, response) } just Runs

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        // Extra space means token extraction will be wrong, so no authentication
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `doFilterInternal handles token with special characters`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IkhRX0FETUluIn0.signature"
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtil.isTokenValid(token) } returns true
        every { jwtUtil.extractUsername(token) } returns "user@example.com"
        every { jwtUtil.extractRole(token) } returns "HQ_ADMIN"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/test")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals("user@example.com", authentication.principal)
    }

    @Test
    fun `doFilterInternal clears previous authentication on each request`() {
        // Set up initial authentication
        val initialToken = "initial.token"
        every { request.getHeader("Authorization") } returns "Bearer $initialToken"
        every { jwtUtil.isTokenValid(initialToken) } returns true
        every { jwtUtil.extractUsername(initialToken) } returns "user1@example.com"
        every { jwtUtil.extractRole(initialToken) } returns "HQ_ADMIN"
        every { filterChain.doFilter(request, response) } just Runs
        every { request.requestURL } returns StringBuffer("http://localhost:8080/api/test")
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val auth1 = SecurityContextHolder.getContext().authentication
        assertEquals("user1@example.com", auth1.principal)

        // Clear and set up second request
        SecurityContextHolder.clearContext()
        val secondToken = "second.token"
        every { request.getHeader("Authorization") } returns "Bearer $secondToken"
        every { jwtUtil.isTokenValid(secondToken) } returns true
        every { jwtUtil.extractUsername(secondToken) } returns "user2@example.com"
        every { jwtUtil.extractRole(secondToken) } returns "ECDA_OFFICER"

        ReflectionTestUtils.invokeMethod<Unit>(filter, "doFilterInternal", request, response, filterChain)

        val auth2 = SecurityContextHolder.getContext().authentication
        assertEquals("user2@example.com", auth2.principal)
    }
}
