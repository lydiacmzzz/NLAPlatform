package com.ecda.platform.service

import com.ecda.platform.model.Centre
import com.ecda.platform.model.UserRole
import com.ecda.platform.repository.CentreRepository
import com.ecda.platform.repository.UserRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

sealed class CentreScope {
    data class OfficerScope(val hqIds: List<Long>) : CentreScope()
    data class AdminScope(val hqId: Long) : CentreScope()
    data class LeaderScope(val centreId: Long) : CentreScope()
}

@Service
class CentreScopeService(
    private val userRepository: UserRepository,
    private val centreRepository: CentreRepository
) {

    fun resolveScope(auth: Authentication): CentreScope {
        val user = userRepository.findByUsername(auth.name)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        return when (user.role) {
            UserRole.ECDA_OFFICER -> {
                val hqIds = userRepository.findAssignedHqIds(user.id)
                CentreScope.OfficerScope(hqIds)
            }
            UserRole.HQ_ADMIN -> {
                val hqId = user.hqId
                    ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "HQ Admin has no assigned HQ")
                CentreScope.AdminScope(hqId)
            }
            UserRole.CENTRE_LEADER -> {
                val centreId = user.centreId
                    ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Centre Leader has no assigned centre")
                CentreScope.LeaderScope(centreId)
            }
        }
    }

    fun toSpecification(scope: CentreScope): Specification<Centre> = when (scope) {
        is CentreScope.OfficerScope -> Specification { root, _, cb ->
            if (scope.hqIds.isEmpty()) cb.disjunction()
            else root.get<Any>("hq").get<Long>("id").`in`(scope.hqIds)
        }
        is CentreScope.AdminScope -> Specification { root, _, cb ->
            cb.equal(root.get<Any>("hq").get<Long>("id"), scope.hqId)
        }
        is CentreScope.LeaderScope -> Specification { root, _, cb ->
            cb.equal(root.get<Long>("id"), scope.centreId)
        }
    }

    fun assertInScope(centreId: Long, scope: CentreScope) {
        val centre = centreRepository.findById(centreId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Centre not found")
        }
        val allowed = when (scope) {
            is CentreScope.OfficerScope -> centre.hq.id in scope.hqIds
            is CentreScope.AdminScope   -> centre.hq.id == scope.hqId
            is CentreScope.LeaderScope  -> centre.id == scope.centreId
        }
        if (!allowed) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
    }
}
