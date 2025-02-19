package org.eduai.educhat.service

import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto)
    fun saveMessageLog(sendMessageRequestDto: SendMessageRequestDto)
    fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto
    fun restoreThread(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto

}
