package org.eduai.educhat.controller.file

import org.eduai.educhat.dto.file.request.DownloadFilesRequestDto
import org.eduai.educhat.service.member.SmartLeadLoginService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/disc/file")
class FileController(
    private val redisTemplate: StringRedisTemplate,
    private val smartLeadLoginService: SmartLeadLoginService
) {

    @PostMapping("/material/download/M")
    fun downloadMultiFilesExceptVideo(
        @RequestBody downloadFilesRequestDto: DownloadFilesRequestDto
    ): ResponseEntity<String> {
        try {
            val userId = downloadFilesRequestDto.userId
            val userPw = downloadFilesRequestDto.userPw
            val fileIds = downloadFilesRequestDto.fileIds

            // 1. 로그인 시도
            val loginSuccess = smartLeadLoginService.login(userId, userPw)
            if (!loginSuccess) {
                return ResponseEntity("Login Failed", HttpStatus.UNAUTHORIZED)
            }

            // 2. 세션 키 생성
            val sessionKey = UUID.randomUUID().toString()
            val redisSessionKey = "file:session:$sessionKey"

            // 3. 세션 정보 Redis에 저장
            val ops = redisTemplate.opsForHash<String, String>()
            ops.put(redisSessionKey, "userId", userId)
            ops.put(redisSessionKey, "password", userPw)
            ops.put(redisSessionKey, "fileIds", fileIds.joinToString(","))
            redisTemplate.expire(redisSessionKey, java.time.Duration.ofMinutes(3))

            // 4. Stream 메시지에는 sessionKey만!
            val message: Map<String, String> = mapOf(
                "sessionKey" to sessionKey
            )

            redisTemplate.opsForStream<String, String>().add("fileDownloadStream", message)
            return ResponseEntity("Success", HttpStatus.OK)

        } catch (ex: Exception) {
            return ResponseEntity("Failed to send message: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @PostMapping("/material/download/V")
    fun downloadMultiFilesOnlyVideo() {
        // 추가 구현 가능
    }
}
