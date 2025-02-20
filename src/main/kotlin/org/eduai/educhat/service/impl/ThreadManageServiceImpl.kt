
package org.eduai.educhat.service.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.MessageDto
import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.dto.response.RestoreThreadResponseDto
import org.eduai.educhat.repository.ClsMstRepository
import org.eduai.educhat.repository.DiscussionGrpMemberRepository
import org.eduai.educhat.service.ChannelManageService
import org.eduai.educhat.service.KeyGeneratorService
import org.eduai.educhat.service.ThreadManageService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val channelManageService: ChannelManageService,
    private val keyGenService: KeyGeneratorService,
    private val clsRepo: ClsMstRepository,
    private val grpMemRepo: DiscussionGrpMemberRepository,
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
        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val clsSessionKey  = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)

        val topicName = redisTemplate.opsForHash<String, String>().get(clsSessionKey, grpSessionKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")

        println(topicName)

        val messageJson = jacksonObjectMapper().writeValueAsString(MessageDto(
            clsId = clsId,
            sender = sendMessageRequestDto.sender,
            grpId = grpId,
            message = sendMessageRequestDto.message,
            timestamp = LocalDateTime.now().toString()
        ))

        saveMessageLog(clsId, grpId, messageJson)

        redisTemplate.convertAndSend(topicName, messageJson)

        println("📤 Redis 전송됨: $messageJson → 채널: $topicName")
    }

    override fun saveMessageLog(clsId: String, grpId: String, messageJson: String) {
        // 현재 청크 번호를 저장하는 키 (없으면 0부터 시작)
        val currentChunkKey = "chat_logs_prefix:$clsId:$grpId:current_chunk"

        // Redis에서 현재 청크 번호를 가져옵니다. (없으면 "0" 사용)
        val chunkIdStr = redisTemplate.opsForValue().get(currentChunkKey) ?: "0"
        val chunkId = chunkIdStr.toIntOrNull() ?: 0

        // clsId와 grpId, 그리고 현재 청크 번호를 기반으로 청크 로그 키를 생성합니다.
        val chunkLogKey = keyGenService.generateChunkNum(clsId, grpId, chunkId.toString())

        // 현재 청크 리스트의 크기를 확인합니다.
        val currentChunkSize = redisTemplate.opsForList().size(chunkLogKey) ?: 0

        if (currentChunkSize >= 100) {
            // 현재 청크가 100개 이상의 메시지를 보유하고 있다면,
            // 새로운 청크 번호로 전환하고 메시지를 새 청크에 저장합니다.
            val newChunkId = chunkId + 1
            redisTemplate.opsForValue().set(currentChunkKey, newChunkId.toString())

            val newChunkLogKey = keyGenService.generateChunkNum(clsId, grpId, newChunkId.toString())
            redisTemplate.opsForList().leftPush(newChunkLogKey, messageJson)
        } else {
            // 현재 청크에 여유가 있다면 그대로 메시지를 저장합니다.
            redisTemplate.opsForList().leftPush(chunkLogKey, messageJson)
        }
    }


    override fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = UUID.fromString(enterThreadRequestDto.grpId)
        val userDiv = enterThreadRequestDto.userDiv

        println("$userId, $clsId, $grpId, $userDiv")

        if(verifyUser(userId, grpId, userDiv, clsId)){
            return EnterThreadResponseDto(
                statusCode = "VERIFIED",
                //grpMem id 를 statusToken 으로 사용
                statusToken = grpMemRepo.findGrpMemByUserIdAndGrpId(userId, grpId)?.id.toString(),
            )
        }else{
            throw IllegalArgumentException("Not valid User")
        }


    }


    //여기 검증 어케할지 생각~
    override fun restoreThread(enterThreadRequestDto: RestoreThreadRequestDto): RestoreThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = enterThreadRequestDto.grpId
        val userDiv = enterThreadRequestDto.userDiv


        println("$userId, $clsId, $grpId, $userDiv")

        if(verifyUser(userId, UUID.fromString(grpId), userDiv, clsId)){
            return RestoreThreadResponseDto(
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

    private fun verifyUser(userId: String, grpId : UUID, userDiv : String, clsId: String) : Boolean {
        return if(userDiv == "O10") {
            if(clsRepo.isUserOwner(clsId, userId)){
                return true
            }else{
                throw IllegalArgumentException("Not Owner for this class")
            }
        }else{
            grpMemRepo.findGrpMemByUserId(userId, grpId) ?: false
        }
    }
}
