package org.eduai.educhat.service

import org.eduai.educhat.dto.request.SendMessageRequestDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto)
    fun saveMessageLog(sendMessageRequestDto: SendMessageRequestDto)

}
