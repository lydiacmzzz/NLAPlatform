package com.ecda.platform.repository

import com.ecda.platform.model.WaiverHistory
import org.springframework.data.jpa.repository.JpaRepository

interface WaiverHistoryRepository : JpaRepository<WaiverHistory, Long> {
    fun findByCentreIdOrderByApprovalDateDesc(centreId: Long): List<WaiverHistory>
}
