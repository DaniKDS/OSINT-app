package com.ptbox.osintbackend.repository

import com.ptbox.osintbackend.entity.ScanResultEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ScanResultRepository : JpaRepository<ScanResultEntity, String> {

    fun findByScanId(scanId: String): ScanResultEntity?
}
