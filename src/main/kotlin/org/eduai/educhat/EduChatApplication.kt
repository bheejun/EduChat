package org.eduai.educhat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication()
class EduChatApplication

fun main(args: Array<String>) {
    runApplication<EduChatApplication>(*args)
}
