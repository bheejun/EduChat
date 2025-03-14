package org.eduai.educhat.controller.discussion

import org.eduai.educhat.dto.discussion.request.TopicHistoryRequestDto
import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.eduai.educhat.entity.DiscTopic
import org.eduai.educhat.service.discussion.TopicManageService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/disc/topic")
class DiscussionTopicController(
    private val topicManageService: TopicManageService
) {

    @PostMapping("/list")
    fun getTopicHistoryList(@RequestBody topicHistoryRequestDto: TopicHistoryRequestDto)
    : Page<TopicHistoryResponseDto> {

        return topicManageService.getTopicHistoryList(topicHistoryRequestDto)
    }
}