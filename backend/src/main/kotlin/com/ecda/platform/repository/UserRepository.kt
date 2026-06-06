package com.ecda.platform.repository

import com.ecda.platform.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean

    @Query("SELECT a.id.hqId FROM OfficerHqAssignment a WHERE a.id.officerId = :userId")
    fun findAssignedHqIds(userId: Long): List<Long>
}
