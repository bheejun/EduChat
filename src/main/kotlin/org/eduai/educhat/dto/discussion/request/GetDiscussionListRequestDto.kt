package org.eduai.educhat.dto.discussion.request

data class GetDiscussionListRequestDto(
    val clsId: String,
    val userId: String,
    val userDiv: String
)
