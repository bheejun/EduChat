package org.eduai.educhat.dto.discussion.response

data class RestoreThreadResponseDto(

    val clsId: String,
    val userId: String,
    val grpId: String,
    val messages: List<String>
)
