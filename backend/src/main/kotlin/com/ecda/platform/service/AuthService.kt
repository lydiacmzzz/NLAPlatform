package com.ecda.platform.service

import com.ecda.platform.dto.LoginRequest
import com.ecda.platform.dto.LoginResponse
import com.ecda.platform.repository.UserRepository
import com.ecda.platform.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    fun login(req: LoginRequest): LoginResponse {
        val user = userRepository.findByUsername(req.username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!user.isActive) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is inactive")
        if (!passwordEncoder.matches(req.password, user.passwordHash))
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        val token = jwtUtil.generateToken(user.username, user.role.name)
        return LoginResponse(token, user.username, user.role, user.fullName)
    }
}
