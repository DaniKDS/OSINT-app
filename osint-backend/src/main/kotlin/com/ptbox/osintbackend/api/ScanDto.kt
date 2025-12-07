package com.ptbox.osintbackend.api

import com.ptbox.osintbackend.domain.ScanStatus
import com.ptbox.osintbackend.domain.ScanTool
import java.time.Instant

data class ScanDto(
    val id: String?,
    val domain: String,
    val tool: ScanTool,
    val status: ScanStatus,
    val startTime: Instant,
    val endTime: Instant?,
    val progress: Int
)
