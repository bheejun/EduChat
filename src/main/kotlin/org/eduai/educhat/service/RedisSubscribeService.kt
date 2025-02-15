package org.eduai.educhat.service

import org.springframework.stereotype.Service

@Service
class RedisSubscribeService {

    fun subscribeSession() {
        println("Subscribed to <" + "eduai" + ">")
    }

    fun receiveMessage(message: String) {
        println("Received <" + message + ">")
    }

}
