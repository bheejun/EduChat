
package org.eduai.educhat.service.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.service.ChannelManageService
import org.eduai.educhat.service.KeyGeneratorService
import org.eduai.educhat.service.ThreadManageService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val channelManageService: ChannelManageService,
    private val keyGenService: KeyGeneratorService
) : ThreadManageService{


    override fun createGroupChannel(clsId: String, groupId: UUID) {

        val sessionListKey = keyGenService.generateRedisSessionKey(clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(groupId.toString())
        val topicName = "chat:$groupId"

        channelManageService.createGroupChannel(sessionGrpKey)
        redisTemplate.opsForHash<String, String>().put(sessionListKey, sessionGrpKey, topicName)

        println("채팅방 생성 완료: $topicName (Group ID: $groupId)")
    }

    override fun removeGroupChannel(clsId: String, groupId: UUID) {

        val sessionListKey = keyGenService.generateRedisSessionKey(clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(groupId.toString())

        channelManageService.removeGroupChannel(sessionGrpKey)

        redisTemplate.opsForHash<String, String>().delete(sessionListKey, sessionGrpKey)

        println("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {

        val sessionListKey = keyGenService.generateRedisSessionKey(sendMessageRequestDto.clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(sendMessageRequestDto.grpId)

        val topicName = redisTemplate.opsForHash<String, String>().get(sessionListKey, sessionGrpKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")

        val messageJson = jacksonObjectMapper().writeValueAsString(sendMessageRequestDto)

        //TODO : 여기에 채팅 로그랑 레디스 리스트에 채팅내역 남기고 DB에 저장하는 로직은 배치로 구현해서 적용하자.
        saveMessageLog(sendMessageRequestDto)

        redisTemplate.convertAndSend(topicName, messageJson)

        println("📤 Redis 전송됨: $messageJson → 채널: $topicName")
    }

    override fun saveMessageLog(sendMessageRequestDto : SendMessageRequestDto) {

        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val logListKeyForRedis = keyGenService.generateRedisLogKey(clsId, grpId)

        val messageJson = jacksonObjectMapper().writeValueAsString(sendMessageRequestDto)

        redisTemplate.opsForList().leftPush(logListKeyForRedis, messageJson)
        redisTemplate.opsForList().trim(logListKeyForRedis, 0, 99)

    }
}
