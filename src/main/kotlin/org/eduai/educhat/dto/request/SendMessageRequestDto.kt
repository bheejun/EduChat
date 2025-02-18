package org.eduai.educhat.dto.request

data class SendMessageRequestDto(
    val sender: String,
    val grpId: String,
    val clsId: String,
    val message: String
) {



}