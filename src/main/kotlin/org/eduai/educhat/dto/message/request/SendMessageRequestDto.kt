package org.eduai.educhat.dto.message.request


data class SendMessageRequestDto(
    val sender: String,
    val senderName: String,
    val grpId: String,
    val clsId: String,
    val message: String
) {

}