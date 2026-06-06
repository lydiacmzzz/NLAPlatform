package com.ecda.platform.repository

import com.ecda.platform.model.Hq
import org.springframework.data.jpa.repository.JpaRepository

interface HqRepository : JpaRepository<Hq, Long> {
    fun findByCode(code: String): Hq?
}
