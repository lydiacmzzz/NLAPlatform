package com.ecda.platform.dto

import com.ecda.platform.model.UserRole
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,
    val role: UserRole,
    val fullName: String?
)
