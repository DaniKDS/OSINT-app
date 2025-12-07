package com.ptbox.osintbackend.service

import com.ptbox.osintbackend.api.ScanUpdateDto
import com.ptbox.osintbackend.domain.ScanStatus
import com.ptbox.osintbackend.domain.ScanTool
import com.ptbox.osintbackend.entity.ScanEntity
import com.ptbox.osintbackend.entity.ScanResultEntity
import com.ptbox.osintbackend.repository.ScanRepository
import com.ptbox.osintbackend.repository.ScanResultRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DockerScanRunner(
    private val scanRepository: ScanRepository,
    private val scanResultRepository: ScanResultRepository,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @Async
    fun runScan(scanId: String, domain: String, tool: ScanTool) {
        val scan = scanRepository.findById(scanId)
            .orElseThrow { IllegalArgumentException("Scan not found: $scanId") }

        try {
            updateProgress(scan, 0, "Starting scan using $tool...")

            val command = when (tool) {
                ScanTool.AMASS -> listOf("docker", "run", "--rm", "caffix/amass", "enum", "-d", domain)
                ScanTool.THE_HARVESTER -> listOf(
                    "docker", "run", "--rm",
                    "custom/theharvester",
                    "-d", domain,
                    "-b", "all"
                )

            }

            updateProgress(scan, 5, "Launching Docker container...")

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            //milestone sequence identical to MockScanRunner
            val milestones = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90)

            //read output async but progress is fixed-timed
            val output = StringBuilder()
            val readerThread = Thread {
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        output.appendLine(line)
                    }
                }
            }
            readerThread.start()
            
            for (p in milestones) {
                Thread.sleep(400)
                updateProgress(scan, p, "Scanning... ($p%)")
            }

            process.waitFor()
            readerThread.join()

            updateProgress(scan, 95, "Parsing results...")

            println("=== RAW OUTPUT START ===")
            println(output.toString())
            println("=== RAW OUTPUT END ===")


            val parsed = parseResults(output.toString(), domain, tool)

            val result = ScanResultEntity(
                scan = scan,
                subdomains = parsed.subdomains,
                ips = parsed.ips,
                emails = parsed.emails,
                linkedInProfiles = parsed.linkedIn
            )

            scanResultRepository.save(result)

            scan.status = ScanStatus.COMPLETED
            scan.endTime = Instant.now()
            scan.progress = 100
            scanRepository.save(scan)

            sendUpdate(scan, "Scan completed", scan.endTime)

        } catch (ex: Exception) {
            scan.status = ScanStatus.FAILED
            scan.endTime = Instant.now()
            scanRepository.save(scan)

            sendUpdate(scan, "Scan failed: ${ex.message}", scan.endTime)
        }
    }

    private fun updateProgress(scan: ScanEntity, progress: Int, msg: String) {
        scan.progress = progress
        scan.status = ScanStatus.RUNNING
        scanRepository.save(scan)
        sendUpdate(scan, msg)
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

    private data class ParsedResults(
        val subdomains: List<String>,
        val ips: List<String>,
        val emails: List<String>,
        val linkedIn: List<String>
    )

    private fun parseResults(output: String, domain: String, tool: ScanTool): ParsedResults {
        val subRegex = Regex("""[a-zA-Z0-9._-]+\.$domain""")
        val ipRegex = Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""")
        val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@$domain""")
        val linkedinRegex = Regex("""https?://[^\s]*linkedin[^\s]*""")

        return ParsedResults(
            subdomains = subRegex.findAll(output).map { it.value }.distinct().toList(),
            ips = ipRegex.findAll(output).map { it.value }.distinct().toList(),
            emails = emailRegex.findAll(output).map { it.value }.distinct().toList(),
            linkedIn = linkedinRegex.findAll(output).map { it.value }.distinct().toList()
        )
    }
}
