package com.ecda.platform.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

enum class WaiverStatus { APPROVED, EXPIRED, SUPERSEDED, REJECTED }

@Entity
@Table(name = "waiver_history")
data class WaiverHistory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id", nullable = false)
    val centre: Centre = Centre(),

    @Column(name = "waiver_type", nullable = false)
    val waiverType: String = "",

    @Column(name = "waiver_title", nullable = false)
    val waiverTitle: String = "",

    @Column(name = "waiver_description")
    val waiverDescription: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "waiver_status", nullable = false)
    val waiverStatus: WaiverStatus = WaiverStatus.APPROVED,

    @Column(name = "approval_date")
    val approvalDate: LocalDate? = null,

    @Column(name = "expiry_date")
    val expiryDate: LocalDate? = null,

    @Column(name = "approved_by")
    val approvedBy: String? = null,

    @Column(name = "officer_remarks")
    val officerRemarks: String? = null,

    @Column(name = "supporting_document_name")
    val supportingDocumentName: String? = null,

    @Column(name = "supporting_document_url")
    val supportingDocumentUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
