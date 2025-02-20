package org.eduai.educhat.service

import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.dto.response.RestoreThreadResponseDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto)
    fun saveMessageLog(clsId: String, grpId : String, messageJson : String)
    fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto
    fun restoreThread(enterThreadRequestDto: RestoreThreadRequestDto) : RestoreThreadResponseDto

}
