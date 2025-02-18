package org.eduai.educhat.dto

data class ReceivedMessageDto(
    val sender: String,
    val grpId: String,
    val clsId: String,
    val message: String
)
