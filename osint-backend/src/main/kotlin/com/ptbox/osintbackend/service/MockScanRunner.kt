package com.ptbox.osintbackend.service

import com.ptbox.osintbackend.api.ScanUpdateDto
import com.ptbox.osintbackend.domain.ScanStatus
import com.ptbox.osintbackend.entity.ScanEntity
import com.ptbox.osintbackend.entity.ScanResultEntity
import com.ptbox.osintbackend.repository.ScanRepository
import com.ptbox.osintbackend.repository.ScanResultRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MockScanRunner(
    private val scanRepository: ScanRepository,
    private val scanResultRepository: ScanResultRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    @Value("\${scan.mock.delay-ms:500}") private val delayMs: Long
) {

    @Async
    fun runScan(scanId: String) {
        val scan = scanRepository.findById(scanId)
            .orElseThrow { IllegalArgumentException("Scan not found: $scanId") }

        try {
            scan.status = ScanStatus.RUNNING
            scan.progress = 0
            scanRepository.save(scan)
            sendUpdate(scan, "Scan started")

            for (p in 10..100 step 10) {
                Thread.sleep(delayMs)
                scan.progress = p
                scanRepository.save(scan)
                sendUpdate(scan, "Progress $p%")
            }

            val result = ScanResultEntity(
                scan = scan,
                subdomains = listOf("www.${scan.domain}", "mail.${scan.domain}"),
                ips = listOf("8.8.8.8", "8.8.4.4"),
                emails = listOf("contact@${scan.domain}"),
                linkedInProfiles = listOf("https://linkedin.com/company/${scan.domain}")
            )

            scanResultRepository.save(result)

            scan.status = ScanStatus.COMPLETED
            scan.endTime = Instant.now()
            scanRepository.save(scan)

            sendUpdate(scan, "Scan completed", scan.endTime)

        } catch (ex: Exception) {
            scan.status = ScanStatus.FAILED
            scan.endTime = Instant.now()
            scanRepository.save(scan)

            sendUpdate(scan, "Scan failed: ${ex.message}", scan.endTime)
        }
    }

    private fun sendUpdate(scan: ScanEntity, message: String, endTime: Instant? = null) {
        val dto = ScanUpdateDto(
            id = scan.id!!,
            status = scan.status,
            progress = scan.progress,
            message = message,
            endTime = endTime
        )
        messagingTemplate.convertAndSend("/topic/scans/${scan.id}", dto)
    }
}