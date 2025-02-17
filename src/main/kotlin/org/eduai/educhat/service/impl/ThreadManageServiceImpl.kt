package org.eduai.educhat.service.impl

import org.eduai.educhat.config.RedisConfig
import org.eduai.educhat.dto.request.RedisMessageRequestDto
import org.eduai.educhat.repository.DiscussionGrpRepository
import org.eduai.educhat.service.RedisSubscribeService
import org.eduai.educhat.service.ThreadManageService
import org.eduai.educhat.service.WebSocketService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisConfig: RedisConfig,
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val redisTemplate: StringRedisTemplate,
    private val webSocketService: WebSocketService,
    private val grpRepo : DiscussionGrpRepository,
    private val subsService: RedisSubscribeService
) : ThreadManageService {

    private fun getSessionListKey(clsId: String): String {
        return "chat_sessions:$clsId"
    }

    override fun createGroupChannel(clsId: String, groupId: UUID) {
        val topicName = "chat:$groupId"

        redisConfig.addChannelForGroup(redisMessageListenerContainer, this, groupId)
        redisConfig.subscribeNewGroupChannel(redisMessageListenerContainer, subsService, groupId.toString())
        val sessionListKey = getSessionListKey(clsId)
        redisTemplate.opsForHash<String, String>().put(sessionListKey, groupId.toString(), topicName)


        println("채팅방 생성: $topicName (Group ID: $groupId)")
    }

    override fun removeGroupChannel(clsId: String, groupId: UUID) {
        redisConfig.removeChannelForGroup(redisMessageListenerContainer, groupId)

        val updateResult = grpRepo.updateGrpStatus(groupId, "DEL")
        if (updateResult == 0) {
            throw IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }
        val sessionListKey = getSessionListKey(clsId)
        redisTemplate.opsForHash<String, String>().delete(sessionListKey, groupId.toString())

        println("채팅방 삭제: (Group ID: $groupId)")
    }

    override fun sendMessage(redisMessageRequestDto: RedisMessageRequestDto) {
        val sessionListKey = getSessionListKey(redisMessageRequestDto.clsId)
        val message = redisMessageRequestDto.message
        val topicName = redisTemplate.opsForHash<String, String>().get(sessionListKey, redisMessageRequestDto.grpId)
            ?: throw IllegalArgumentException("")

        redisTemplate.convertAndSend(topicName, message)

        println("Redis 전송됨: $message → 채널: $topicName")
    }

    override fun receiveMessage(groupId: UUID, message: String) {
//        val topicName = redisTemplate.opsForHash<String, String>().get(sessionListKey, groupId.toString())
//            ?: throw IllegalArgumentException("🚫 유효하지 않은 채널입니다!")
//
//        webSocketService.sendMessageToClients(topicName, message)
    }


}
