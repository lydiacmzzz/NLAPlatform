package com.ecda.platform.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

enum class CentreType { INFANT_CARE, STUDENT_CARE, ANCHOR_OPERATOR, PARTNER_OPERATOR }
enum class LicenceStatus { ACTIVE, PENDING_RENEWAL, SUSPENDED, EXPIRED }

@Entity
@Table(name = "centres")
data class Centre(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "centre_id", nullable = false, unique = true)
    val centreId: String = "",

    @Column(name = "licence_number", nullable = false, unique = true)
    val licenceNumber: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "centre_type", nullable = false)
    var centreType: CentreType = CentreType.INFANT_CARE,

    @Column(nullable = false)
    var address: String = "",

    @Column(name = "postal_code", nullable = false)
    var postalCode: String = "",

    @Column(name = "operating_hours")
    var operatingHours: String? = null,

    @Column(nullable = false)
    var capacity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "licence_status", nullable = false)
    var licenceStatus: LicenceStatus = LicenceStatus.PENDING_RENEWAL,

    @Column(name = "licence_issue_date")
    var licenceIssueDate: LocalDate? = null,

    @Column(name = "licence_expiry_date")
    var licenceExpiryDate: LocalDate? = null,

    @Column(name = "renewal_due_date")
    var renewalDueDate: LocalDate? = null,

    @Column(name = "application_stage")
    var applicationStage: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_id", nullable = false)
    var hq: Hq = Hq(),

    @OneToMany(mappedBy = "centre", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val kahDetails: MutableList<KahDetail> = mutableListOf(),

    @OneToMany(mappedBy = "centre", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val contacts: MutableList<CentreContact> = mutableListOf(),

    @OneToMany(mappedBy = "centre", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val lifecycleEvents: MutableList<CentreLifecycleEvent> = mutableListOf()
)
