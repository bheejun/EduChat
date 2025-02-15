package org.eduai.educhat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class EduChatApplication

fun main(args: Array<String>) {
    runApplication<EduChatApplication>(*args)
}
