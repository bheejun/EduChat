package org.eduai.educhat.dto.discussion.response

import java.time.LocalDateTime

data class TopicHistoryResponseDto(
    val sessionId : String,
    var userId: String,
    var clsId: String,
    var topicContent: MutableMap<String, Any>,
    var insDt: LocalDateTime
)
