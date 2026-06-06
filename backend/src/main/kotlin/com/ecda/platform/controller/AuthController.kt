package com.ecda.platform.controller

import com.ecda.platform.dto.LoginRequest
import com.ecda.platform.dto.LoginResponse
import com.ecda.platform.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): LoginResponse = authService.login(req)
}
