package com.ptbox.osintbackend.service

import com.ptbox.osintbackend.api.CreateScanRequest
import com.ptbox.osintbackend.api.ScanDto
import com.ptbox.osintbackend.api.ScanResultDto
import com.ptbox.osintbackend.api.ScanWithResultDto
import com.ptbox.osintbackend.entity.ScanEntity
import com.ptbox.osintbackend.entity.ScanResultEntity
import com.ptbox.osintbackend.repository.ScanRepository
import com.ptbox.osintbackend.repository.ScanResultRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScanService(
    private val scanRepository: ScanRepository,
    private val scanResultRepository: ScanResultRepository,
    private val mockScanRunner: MockScanRunner
    //private val dockerScanRunner: DockerScanRunner
) {

    @Transactional
    fun createScan(request: CreateScanRequest): ScanDto {
        val scan = ScanEntity(
            domain = request.domain,
            tool = request.tool
        )

        val saved = scanRepository.saveAndFlush(scan)

        mockScanRunner.runScan(saved.id!!)
        // NOTE: Using mockScanRunner instead of dockerScanRunner because the official
        // theHarvester Docker image is currently broken (entrypoint failure) and cannot be
        // executed reliably inside our containerized environment. The image fails to run due
        // to missing dependencies and incorrect script paths, causing the container to exit
        // immediately. To keep the application functional end-to-end and allow UI development
        // and scan lifecycle testing, the mock implementation is used as a temporary fallback.

        //dockerScanRunner.runScan(
        //scanId = saved.id!!,
        //domain = saved.domain,
        //tool = saved.tool
        //)
        
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun getAllScans(): List<ScanDto> =
        scanRepository.findAll()
            .sortedByDescending { it.startTime }
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun getScanWithResult(scanId: String): ScanWithResultDto {
        val scan = scanRepository.findById(scanId)
            .orElseThrow { IllegalArgumentException("Scan not found with id=$scanId") }

        val result = scanResultRepository.findByScanId(scanId)

        return ScanWithResultDto(
            scan = scan.toDto(),
            result = result?.toDto()
        )
    }

    private fun ScanEntity.toDto() = ScanDto(
        id = id,
        domain = domain,
        tool = tool,
        status = status,
        startTime = startTime,
        endTime = endTime,
        progress = progress
    )

    private fun ScanResultEntity.toDto() = ScanResultDto(
        subdomains = subdomains,
        ips = ips,
        emails = emails,
        linkedInProfiles = linkedInProfiles
    )
}

