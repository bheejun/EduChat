package org.eduai.educhat.dto.discussion.request

data class RestoreThreadRequestDto(

    val clsId: String,
    val userId: String,
    val userName: String,
    val grpId: String,
    val userDiv: String,
    val lastMessageTimestamp: String? = null
)
