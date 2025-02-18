package org.eduai.educhat

import org.eduai.educhat.security.SecurityConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication()
class EduChatApplication

fun main(args: Array<String>) {
    runApplication<EduChatApplication>(*args)
}
