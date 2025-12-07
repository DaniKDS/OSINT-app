package com.ptbox.osintbackend.api

import com.ptbox.osintbackend.domain.ScanStatus
import java.time.Instant

data class ScanUpdateDto(
    val id: String?,
    val status: ScanStatus,
    val progress: Int,
    val message: String? = null,
    val endTime: Instant? = null
)
