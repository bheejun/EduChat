package org.eduai.educhat.dto


data class MessageDto(
    val sender: String,
    val grpId: String,
    val clsId: String,
    val message: String,
    val timestamp : String
)
