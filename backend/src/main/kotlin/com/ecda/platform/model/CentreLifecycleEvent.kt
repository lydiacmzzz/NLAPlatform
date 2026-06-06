package com.ecda.platform.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "centre_lifecycle_events")
data class CentreLifecycleEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id", nullable = false)
    val centre: Centre = Centre(),

    @Column(name = "event_type", nullable = false)
    val eventType: String = "",

    @Column(nullable = false)
    val description: String = "",

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "recorded_by", nullable = false)
    val recordedBy: String = ""
)
