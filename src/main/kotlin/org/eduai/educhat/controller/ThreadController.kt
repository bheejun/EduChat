package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.service.ThreadManageService
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

    @PostMapping("/enter")
    fun verifyUser(@RequestBody request: EnterThreadRequestDto) :ResponseEntity<EnterThreadResponseDto> {

        println(request.userId + "님이 " + request.clsId + "의 " + request.grpId + "에 입장하였습니다.")

        return ResponseEntity(threadManageService.enterChannel(request), HttpStatus.OK)

    }
    //채팅방 입장시 대화내용 불러오기
    @PostMapping("/restore")
    fun restoreChat(@RequestBody request: EnterThreadRequestDto) :ResponseEntity<EnterThreadResponseDto> {


        println(  request.userId + "님이 " + request.clsId + "의 " + request.grpId + "에 입장하였습니다.")

        val restoreResult= threadManageService.restoreThread(request)

        return ResponseEntity(restoreResult, HttpStatus.OK)

    }

}