package org.eduai.educhat.dto.response

data class RestoreThreadResponseDto(

    val clsId: String,
    val userId: String,
    val grpId: String,
    val messages: List<String>
)
