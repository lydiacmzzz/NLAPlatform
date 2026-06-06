package com.ecda.platform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EcdaApplication

fun main(args: Array<String>) {
    runApplication<EcdaApplication>(*args)
}
