
package org.eduai.educhat.service.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.repository.DiscussionGrpMemberRepository
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
    private val keyGenService: KeyGeneratorService,
    private val grpMemRepo: DiscussionGrpMemberRepository
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

        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(groupId.toString())

        channelManageService.removeGroupChannel(grpSessionKey)

        redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)

        println("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {

        val clsSessionKey  = keyGenService.generateRedisSessionKey(sendMessageRequestDto.clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(sendMessageRequestDto.grpId)

        val topicName = redisTemplate.opsForHash<String, String>().get(clsSessionKey, grpSessionKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")

        println(topicName)

        val messageJson = jacksonObjectMapper().writeValueAsString(sendMessageRequestDto)

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

    override fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = enterThreadRequestDto.grpId

        if(verifyUser(userId, UUID.fromString(grpId))){
            return EnterThreadResponseDto(
                userId = userId,
                clsId = clsId,
                grpId = grpId,
                messages =  redisTemplate.opsForList().range(
                    keyGenService.generateRedisLogKey(clsId, grpId), 0, 99)
                    ?: listOf("Empty Session")
            )
        }else{
            throw IllegalArgumentException("Not valid User")
        }


    }

    override fun restoreThread(enterThreadRequestDto: EnterThreadRequestDto): EnterThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = enterThreadRequestDto.grpId

        if(verifyUser(userId, UUID.fromString(grpId))){
            return EnterThreadResponseDto(
                userId = userId,
                clsId = clsId,
                grpId = grpId,
                messages =  redisTemplate.opsForList().range(
                    keyGenService.generateRedisLogKey(clsId, grpId), 0, 99)
                    ?: listOf("Empty Session")
            )
        }else{
            throw IllegalArgumentException("Not valid User")
        }
    }

    private fun verifyUser(userId: String, grpId : UUID) : Boolean {

        return grpMemRepo.findGrpMemByUserId(userId, grpId) ?: false

    }
}
