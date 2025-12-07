package com.ptbox.osintbackend.api

data class ScanWithResultDto(
    val scan: ScanDto,
    val result: ScanResultDto?
)
