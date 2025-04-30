package org.eduai.educhat.controller.discussion

import org.eduai.educhat.dto.discussion.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.discussion.response.GetDiscussionListResponseDto
import org.eduai.educhat.service.discussion.DiscussionHistoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/disc/history")
class DiscussionHistoryController(
    private val discussionHistoryService: DiscussionHistoryService
) {

    @PostMapping("/list")
    fun getDiscHistoryList(@RequestBody getDiscussionListRequestDto: GetDiscussionListRequestDto
    ): ResponseEntity<GetDiscussionListResponseDto> {
        val response = GetDiscussionListResponseDto(
            discussionList = discussionHistoryService.getDiscHistList(getDiscussionListRequestDto)
        )
        return ResponseEntity<GetDiscussionListResponseDto>(response, HttpStatus.OK)
    }

    @PostMapping("/stats")
    fun getDiscHistoryStats(){

    }

}