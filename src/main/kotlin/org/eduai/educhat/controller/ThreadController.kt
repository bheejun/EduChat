package org.eduai.educhat.controller

import org.eduai.educhat.controller.ThreadController.Companion.logger
import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.dto.response.RestoreThreadResponseDto
import org.eduai.educhat.service.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/discussion/thread")
class ThreadController(
    private val threadManageService: ThreadManageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ThreadController::class.java)
    }

    @PostMapping("/enter")
    fun verifyUser(@RequestBody request: EnterThreadRequestDto) :ResponseEntity<EnterThreadResponseDto> {
        logger.info(request.userId + "님이 " + request.clsId + "의 " + request.grpId + "에 입장하였습니다.")
        return ResponseEntity(threadManageService.enterChannel(request), HttpStatus.OK)

    }
    //채팅방 입장시 대화내용 불러오기
    @PostMapping("/restore")
    fun restoreChat(@RequestBody request: RestoreThreadRequestDto) :ResponseEntity<RestoreThreadResponseDto> {
        return ResponseEntity(threadManageService.restoreThread(request), HttpStatus.OK)

    }

}