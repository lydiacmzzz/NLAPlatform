package com.ecda.platform.repository

import com.ecda.platform.model.KahDetail
import org.springframework.data.jpa.repository.JpaRepository

interface KahDetailRepository : JpaRepository<KahDetail, Long> {
    fun findByCentreIdOrderByCreatedAtDesc(centreId: Long): List<KahDetail>
    fun findByCentreIdAndIsCurrentTrue(centreId: Long): KahDetail?
}
