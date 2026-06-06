package com.ecda.platform.service

import com.ecda.platform.dto.LoginRequest
import com.ecda.platform.model.User
import com.ecda.platform.model.UserRole
import com.ecda.platform.repository.UserRepository
import com.ecda.platform.security.JwtUtil
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val jwtUtil: JwtUtil = mockk()
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(userRepository, passwordEncoder, jwtUtil)
    }

    @Test
    fun `login returns token for valid credentials`() {
        val user = User(
            id = 1L, username = "officer1", email = "o@ecda.gov.sg",
            passwordHash = "hashed", role = UserRole.ECDA_OFFICER, fullName = "Officer One", isActive = true
        )
        every { userRepository.findByUsername("officer1") } returns user
        every { passwordEncoder.matches("pass123", "hashed") } returns true
        every { jwtUtil.generateToken("officer1", "ECDA_OFFICER") } returns "jwt-token"

        val result = authService.login(LoginRequest("officer1", "pass123"))

        assertEquals("jwt-token", result.token)
        assertEquals(UserRole.ECDA_OFFICER, result.role)
    }

    @Test
    fun `login throws UNAUTHORIZED for unknown user`() {
        every { userRepository.findByUsername("unknown") } returns null

        assertThrows<ResponseStatusException> { authService.login(LoginRequest("unknown", "pass")) }
    }

    @Test
    fun `login throws UNAUTHORIZED for wrong password`() {
        val user = User(
            id = 1L, username = "officer1", email = "o@ecda.gov.sg",
            passwordHash = "hashed", role = UserRole.ECDA_OFFICER, isActive = true
        )
        every { userRepository.findByUsername("officer1") } returns user
        every { passwordEncoder.matches("wrong", "hashed") } returns false

        assertThrows<ResponseStatusException> { authService.login(LoginRequest("officer1", "wrong")) }
    }

    @Test
    fun `login throws UNAUTHORIZED for inactive user`() {
        val user = User(
            id = 1L, username = "officer1", email = "o@ecda.gov.sg",
            passwordHash = "hashed", role = UserRole.ECDA_OFFICER, isActive = false
        )
        every { userRepository.findByUsername("officer1") } returns user

        assertThrows<ResponseStatusException> { authService.login(LoginRequest("officer1", "pass")) }
    }
}
