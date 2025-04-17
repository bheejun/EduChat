package org.eduai.educhat.service.discussion

import org.eduai.educhat.dto.discussion.request.*
import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.dto.message.request.SendMessageRequestDto
import org.eduai.educhat.dto.discussion.response.EnterThreadResponseDto
import org.eduai.educhat.dto.discussion.response.RestoreThreadResponseDto
import org.eduai.educhat.dto.discussion.response.SearchResponseDto
import org.eduai.educhat.dto.discussion.response.ThreadAccessResponseDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto)
    fun saveMessageLog(clsId: String, grpId : String, messageDto: MessageDto)
    fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto
    fun restoreThread(restoreRequest: RestoreThreadRequestDto) : RestoreThreadResponseDto
    fun pauseThread(pauseRequest: PauseThreadRequestDto) :String
    fun restartThread(restartRequest: RestartThreadRequestDto) :String
    fun checkAccess(threadAccessRequestDto: ThreadAccessRequestDto) : ThreadAccessResponseDto
    fun searchOnThread(searchRequestDto: SearchRequestDto): SearchResponseDto

    fun addTestMessages(request: AddTestMessagesRequestDto)

}
