package com.ptbox.osintbackend.controller

import com.ptbox.osintbackend.api.CreateScanRequest
import com.ptbox.osintbackend.api.ScanDto
import com.ptbox.osintbackend.api.ScanWithResultDto
import com.ptbox.osintbackend.service.ScanService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/scans")
class ScanController(
    private val scanService: ScanService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createScan(@RequestBody request: CreateScanRequest): ScanDto {
        //todo validation
        return scanService.createScan(request)
    }

    @GetMapping
    fun getAllScans(): List<ScanDto> {
        return scanService.getAllScans()
    }

    @GetMapping("/{id}")
    fun getScan(@PathVariable id: String): ScanWithResultDto {
        return scanService.getScanWithResult(id)
    }
}
