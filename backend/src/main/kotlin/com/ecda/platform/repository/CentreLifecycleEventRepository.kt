package com.ecda.platform.repository

import com.ecda.platform.model.CentreLifecycleEvent
import org.springframework.data.jpa.repository.JpaRepository

interface CentreLifecycleEventRepository : JpaRepository<CentreLifecycleEvent, Long> {
    fun findByCentreIdOrderByOccurredAtDesc(centreId: Long): List<CentreLifecycleEvent>
}
