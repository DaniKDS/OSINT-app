package com.ptbox.osintbackend.api

import com.ptbox.osintbackend.domain.ScanTool

data class CreateScanRequest(
    val domain: String,
    val tool: ScanTool
)
