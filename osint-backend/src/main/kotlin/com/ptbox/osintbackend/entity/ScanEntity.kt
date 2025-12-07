package com.ptbox.osintbackend.entity

import com.ptbox.osintbackend.domain.ScanStatus
import com.ptbox.osintbackend.domain.ScanTool
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "scans")
class ScanEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String? = null,

    @Column(nullable = false)
    var domain: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tool: ScanTool,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ScanStatus = ScanStatus.RUNNING,

    @Column(nullable = false)
    var startTime: Instant = Instant.now(),

    var endTime: Instant? = null,

    @Column(nullable = false)
    var progress: Int = 0
)
