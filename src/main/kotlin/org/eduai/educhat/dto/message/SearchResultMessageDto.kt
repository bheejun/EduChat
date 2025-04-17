package org.eduai.educhat.dto.message

data class SearchResultMessageDto(
    val msgId: Long,
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp : String
)
