package org.eduai.educhat.service

import org.eduai.educhat.dto.request.RedisMessageRequestDto
import java.util.UUID

interface ThreadManageService {
    fun createGroupChannel(clsId: String, groupId: UUID)
    fun removeGroupChannel(clsId: String, groupId: UUID)
    fun sendMessage(redisMessageRequestDto: RedisMessageRequestDto)
    fun receiveMessage(groupId: UUID, message: String)
}
