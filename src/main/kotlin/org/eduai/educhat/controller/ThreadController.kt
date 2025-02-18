package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.EnterRequestDto
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

    //입장 전 검증 로직 검증 후 result 기반으로 입장여부 결정 후 입장시켜주면 됨
    @PostMapping("/verify")
    fun verifyUser(@RequestBody request: EnterRequestDto) :ResponseEntity<String> {

        val verifyResult = request.userId + "님이 " + request.clsId + "의 " + request.grpId + "에 입장하였습니다."
        println(verifyResult)

        return ResponseEntity(verifyResult, HttpStatus.OK)

    }
    //채팅방 재 입장시 대화내용 불러오기
    @PostMapping("/restore")
    fun restoreChat() {

    }

}