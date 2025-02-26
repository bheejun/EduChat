package org.eduai.educhat.dto.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime


@JsonIgnoreProperties(ignoreUnknown = true)
data class RedisMessageDto(
    val sender: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

