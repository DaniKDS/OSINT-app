package com.ptbox.osintbackend

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<OsintBackendApplication>().with(TestcontainersConfiguration::class).run(*args)
}
