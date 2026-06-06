package com.ecda.platform.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "kah_details")
data class KahDetail(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id", nullable = false)
    val centre: Centre = Centre(),

    @Column(name = "principal_name", nullable = false)
    var principalName: String = "",

    @Column(nullable = false)
    var nric: String = "",

    var email: String? = null,
    var phone: String? = null,

    @Column(name = "licence_conditions")
    var licenceConditions: String? = null,

    @Column(name = "appointment_start_date", nullable = false)
    var appointmentStartDate: LocalDate = LocalDate.now(),

    @Column(name = "appointment_end_date")
    var appointmentEndDate: LocalDate? = null,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean = true,

    @Column(name = "pending_approval", nullable = false)
    var pendingApproval: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
