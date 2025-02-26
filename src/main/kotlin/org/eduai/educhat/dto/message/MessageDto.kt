package org.eduai.educhat.dto.message


data class MessageDto(
    val sender: String,
    val grpId: String,
    val clsId: String,
    val message: String,
    val timestamp : String
)
