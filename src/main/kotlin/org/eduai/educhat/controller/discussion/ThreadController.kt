package org.eduai.educhat.controller.discussion

import org.eduai.educhat.dto.discussion.request.*
import org.eduai.educhat.dto.discussion.response.EnterThreadResponseDto
import org.eduai.educhat.dto.discussion.response.RestoreThreadResponseDto
import org.eduai.educhat.dto.discussion.response.SearchResponseDto
import org.eduai.educhat.dto.discussion.response.ThreadAccessResponseDto
import org.eduai.educhat.service.discussion.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/disc/thread")
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
    fun restoreThread(@RequestBody request: RestoreThreadRequestDto) :ResponseEntity<RestoreThreadResponseDto> {
        return ResponseEntity(threadManageService.restoreThread(request), HttpStatus.OK)

    }

    //전체 기능 정지 함수
    //채팅방 , 입장 불가 및 이용자들 밖으로 이동, restart 후에 다시 이용 가능
    @PostMapping("/pause")
    fun pauseThread(@RequestBody request: PauseThreadRequestDto): ResponseEntity<String>{
        return ResponseEntity(threadManageService.pauseThread(request), HttpStatus.OK)
    }

    //pause 해제 함수
    @PostMapping("/restart")
    fun restartThread(@RequestBody request: RestartThreadRequestDto): ResponseEntity<String>{
        return ResponseEntity(threadManageService.restartThread(request), HttpStatus.OK)
    }

    //채팅방 종료 후 history 로 넘기기
    @PostMapping("/close")
    fun closeThread(@RequestBody request: PauseThreadRequestDto):ResponseEntity<String>{
        return ResponseEntity(threadManageService.closeThread(request), HttpStatus.OK)
    }

    @PostMapping("/access")
    fun requestAccessThread(@RequestBody request : ThreadAccessRequestDto): ResponseEntity<ThreadAccessResponseDto>{
        return ResponseEntity(threadManageService.checkAccess(request), HttpStatus.OK)
    }

    @PostMapping("/search")
    fun searchOnThread(@RequestBody searchRequestDto: SearchRequestDto): ResponseEntity<SearchResponseDto>{
        return ResponseEntity(threadManageService.searchOnThread(searchRequestDto), HttpStatus.OK)
    }

    @PostMapping("/test/add-messages")
    fun addTestMessages(@RequestBody request: AddTestMessagesRequestDto): ResponseEntity<String> {
        try {
            threadManageService.addTestMessages(request)
            val message = "Successfully added ${request.count} test messages for clsId=${request.clsId}, grpId=${request.grpId}."
            logger.info("✅ $message")
            return ResponseEntity.ok(message)
        } catch (e: Exception) {
            logger.error("🚨 Error adding test messages: ", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding test messages: ${e.message}")
        }
    }

}