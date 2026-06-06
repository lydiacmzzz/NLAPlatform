package com.ecda.platform.repository

import com.ecda.platform.model.Centre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface CentreRepository : JpaRepository<Centre, Long>, JpaSpecificationExecutor<Centre> {

    fun findByCentreId(centreId: String): Centre?
    fun findByLicenceNumber(licenceNumber: String): Centre?
    fun existsByCentreId(centreId: String): Boolean
    fun existsByLicenceNumber(licenceNumber: String): Boolean
}
