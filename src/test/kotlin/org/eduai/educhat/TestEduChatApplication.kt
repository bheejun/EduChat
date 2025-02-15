package org.eduai.educhat

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<EduChatApplication>().with(TestcontainersConfiguration::class).run(*args)
}
