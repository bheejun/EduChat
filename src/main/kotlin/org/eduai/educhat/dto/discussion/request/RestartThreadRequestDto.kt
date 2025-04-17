package org.eduai.educhat.dto.discussion.request

data class RestartThreadRequestDto(
    val clsId: String,
    val userId: String,
    val grpId: String,
)
