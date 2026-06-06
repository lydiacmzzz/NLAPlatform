package com.ecda.platform.model

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class UserRole { ECDA_OFFICER, HQ_ADMIN, CENTRE_LEADER }

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String = "",

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.ECDA_OFFICER,

    @Column(name = "full_name")
    val fullName: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "hq_id")
    val hqId: Long? = null,

    @Column(name = "centre_id")
    val centreId: Long? = null
)
