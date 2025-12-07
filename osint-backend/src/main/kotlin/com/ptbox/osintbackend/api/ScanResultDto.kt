package com.ptbox.osintbackend.api

data class ScanResultDto(
    val subdomains: List<String>,
    val ips: List<String>,
    val emails: List<String>,
    val linkedInProfiles: List<String>
)
