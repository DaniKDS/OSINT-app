package com.ptbox.osintbackend.entity

import jakarta.persistence.*

@Entity
@Table(name = "scan_results")
class ScanResultEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String? = null,

    @OneToOne
    @JoinColumn(name = "scan_id", nullable = false)
    var scan: ScanEntity,

    @ElementCollection
    @CollectionTable(name = "scan_subdomains", joinColumns = [JoinColumn(name = "scan_id")])
    @Column(name = "subdomain")
    var subdomains: List<String> = emptyList(),

    @ElementCollection
    @CollectionTable(name = "scan_ips", joinColumns = [JoinColumn(name = "scan_id")])
    @Column(name = "ip")
    var ips: List<String> = emptyList(),

    @ElementCollection
    @CollectionTable(name = "scan_emails", joinColumns = [JoinColumn(name = "scan_id")])
    @Column(name = "email")
    var emails: List<String> = emptyList(),

    @ElementCollection
    @CollectionTable(name = "scan_linkedin_profiles", joinColumns = [JoinColumn(name = "scan_id")])
    @Column(name = "linkedin_profile")
    var linkedInProfiles: List<String> = emptyList()
)







