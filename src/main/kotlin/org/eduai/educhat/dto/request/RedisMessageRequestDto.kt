package org.eduai.educhat.dto.request

data class RedisMessageRequestDto(
    val sender: String,
    val sessionId: String,
    val receiver: String,
    val mention: String,
    val message: String
) {



}