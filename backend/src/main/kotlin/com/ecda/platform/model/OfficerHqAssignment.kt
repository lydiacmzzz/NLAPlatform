package com.ecda.platform.model

import jakarta.persistence.*
import java.io.Serializable

@Embeddable
data class OfficerHqAssignmentId(
    val officerId: Long = 0,
    val hqId: Long = 0
) : Serializable

@Entity
@Table(name = "officer_hq_assignments")
data class OfficerHqAssignment(
    @EmbeddedId
    val id: OfficerHqAssignmentId = OfficerHqAssignmentId()
)
