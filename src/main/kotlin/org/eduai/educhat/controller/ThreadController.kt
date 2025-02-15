package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.RedisMessageRequestDto
import org.eduai.educhat.dto.response.RedisMessageResponseDto
import org.eduai.educhat.service.RedisPublishService
import org.eduai.educhat.service.RedisSubscribeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/discussion/thread")
class ThreadController(
    private val redisPublishService: RedisPublishService,
    private val redisSubscribeService: RedisSubscribeService
) {

    @PostMapping("/publish")
    fun publishMessage(@RequestBody redisMessageRequestDto: RedisMessageRequestDto): ResponseEntity<RedisMessageResponseDto> {

        redisPublishService.publishMessage(redisMessageRequestDto.message)


        return ResponseEntity<RedisMessageResponseDto>(RedisMessageResponseDto(), HttpStatus.OK)
    }

    @PostMapping("/subscribe")
    fun subscribeSession(@RequestBody message: RedisMessageRequestDto): ResponseEntity<RedisMessageResponseDto> {
        redisSubscribeService.subscribeSession()
        return ResponseEntity<RedisMessageResponseDto>(RedisMessageResponseDto(), HttpStatus.OK)
    }
}