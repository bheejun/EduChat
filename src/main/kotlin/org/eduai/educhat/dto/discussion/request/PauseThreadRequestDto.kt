package org.eduai.educhat.dto.discussion.request

data class PauseThreadRequestDto(
    val clsId: String,
    val userId: String,
    val grpId: String,
)
