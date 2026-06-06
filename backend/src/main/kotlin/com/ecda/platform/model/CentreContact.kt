package com.ecda.platform.model

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class ContactType { PRIMARY, HQ_LIAISON, EMERGENCY }

@Entity
@Table(name = "centre_contacts")
data class CentreContact(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id", nullable = false)
    val centre: Centre = Centre(),

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false)
    var contactType: ContactType = ContactType.PRIMARY,

    @Column(name = "contact_name", nullable = false)
    var contactName: String = "",

    var role: String? = null,
    var email: String? = null,
    var phone: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
