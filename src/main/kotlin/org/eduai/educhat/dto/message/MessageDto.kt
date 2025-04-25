package org.eduai.educhat.dto.message

import org.eduai.educhat.entity.DiscThreadHist
import java.time.Instant
import java.time.ZoneId
import java.util.*


data class MessageDto(
    val msgId: String,
    val sender: String,
    val senderName: String,
    val grpId: String,
    val clsId: String,
    val message: String,
    val timestamp : String
){
    fun messageDtoToEntity(): DiscThreadHist{
        return DiscThreadHist(
            id = UUID.fromString(msgId),
            clsId = clsId,
            grpId = UUID.fromString(grpId),
            userId = sender,
            userName = senderName,
            insDt = Instant.parse(this.timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime(),
            msg = message

        )
    }
}
