package org.eduai.educhat.service.discussion

import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.dto.discussion.request.EnterThreadRequestDto
import org.eduai.educhat.dto.discussion.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.message.request.SendMessageRequestDto
import org.eduai.educhat.dto.discussion.response.EnterThreadResponseDto
import org.eduai.educhat.dto.discussion.response.RestoreThreadResponseDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto)
    fun saveMessageLog(clsId: String, grpId : String, messageDto: MessageDto)
    fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto
    fun restoreThread(enterThreadRequestDto: RestoreThreadRequestDto) : RestoreThreadResponseDto

}
