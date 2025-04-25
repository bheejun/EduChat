package org.eduai.educhat.dto.message

import java.util.UUID

data class SearchResultMessageDto(
    val msgId: UUID,
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp : String
)
