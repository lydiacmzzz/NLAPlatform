package com.ecda.platform.controller

import com.ecda.platform.dto.*
import com.ecda.platform.service.CentreService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/centres")
class CentreController(private val centreService: CentreService) {

    @GetMapping
    fun searchCentres(
        @RequestParam query: String?,
        @RequestParam centreType: String?,
        @RequestParam licenceStatus: String?,
        @RequestParam renewalDueBefore: String?,
        @RequestParam(defaultValue = "updatedAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        auth: Authentication
    ): PagedResponse<CentreSummaryDto> {
        val req = CentreSearchRequest(
            query = query,
            centreType = centreType?.let { com.ecda.platform.model.CentreType.valueOf(it) },
            licenceStatus = licenceStatus?.let { com.ecda.platform.model.LicenceStatus.valueOf(it) },
            renewalDueBefore = renewalDueBefore?.let { java.time.LocalDate.parse(it) },
            sortBy = sortBy,
            sortDir = sortDir,
            page = page,
            size = size
        )
        return centreService.searchCentres(req, auth)
    }

    @GetMapping("/{id}")
    fun getCentre(@PathVariable id: Long, auth: Authentication): CentreProfileDto =
        centreService.getCentre(id, auth)

    @GetMapping("/by-centre-id/{centreId}")
    fun getCentreByCentreId(@PathVariable centreId: String, auth: Authentication): CentreProfileDto =
        centreService.getCentreByCentreId(centreId, auth)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCentre(@Valid @RequestBody req: CreateCentreRequest, auth: Authentication): CentreProfileDto =
        centreService.createCentre(req, auth.name)

    @PatchMapping("/{id}")
    fun updateCentre(
        @PathVariable id: Long,
        @RequestBody req: UpdateCentreRequest,
        auth: Authentication
    ): CentreProfileDto = centreService.updateCentre(id, req, auth.name, auth)

    @GetMapping("/{centreId}/kah")
    fun getKahHistory(@PathVariable centreId: Long, auth: Authentication): List<KahDetailDto> =
        centreService.getKahHistory(centreId, auth)

    @PostMapping("/{centreId}/kah")
    @ResponseStatus(HttpStatus.CREATED)
    fun addKah(
        @PathVariable centreId: Long,
        @Valid @RequestBody req: CreateKahRequest,
        auth: Authentication
    ): KahDetailDto = centreService.addKah(centreId, req, auth.name, auth)

    @PatchMapping("/{centreId}/kah/{kahId}")
    fun updateKah(
        @PathVariable centreId: Long,
        @PathVariable kahId: Long,
        @RequestBody req: UpdateKahRequest,
        auth: Authentication
    ): KahDetailDto = centreService.updateKah(centreId, kahId, req, auth.name, auth)

    @GetMapping("/{centreId}/waivers")
    fun getWaiverHistory(@PathVariable centreId: Long, auth: Authentication): List<WaiverHistoryDto> =
        centreService.getWaiverHistory(centreId, auth)
}
