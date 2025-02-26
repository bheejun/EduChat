package org.eduai.educhat.dto.discussion.request

data class EnterThreadRequestDto(
    val clsId: String,
    val userId: String,
    val grpId: String,
    val userDiv: String
)
