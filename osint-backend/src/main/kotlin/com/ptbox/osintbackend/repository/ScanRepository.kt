package com.ptbox.osintbackend.repository

import com.ptbox.osintbackend.entity.ScanEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ScanRepository : JpaRepository<ScanEntity, String>
