package org.eduai.educhat.dto.discussion.request

data class ThreadAccessRequestDto(
    val clsId : String,
    val grpId : String,
    val userDiv : String,
    val userId : String
)
