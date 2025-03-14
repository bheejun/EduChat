package org.eduai.educhat.service.discussion

import org.eduai.educhat.dto.discussion.request.TopicHistoryRequestDto
import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.eduai.educhat.entity.DiscTopic
import org.eduai.educhat.repository.DiscTopicRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TopicManageService(
    private val topicRepo : DiscTopicRepository
) {

    fun getTopicHistoryList(topicHistoryRequestDto: TopicHistoryRequestDto) : Page<TopicHistoryResponseDto> {

        val pageable = PageRequest.of(topicHistoryRequestDto.pageNum, topicHistoryRequestDto.pageSize)
        val clsId = topicHistoryRequestDto.clsId

        return if(topicHistoryRequestDto.onlyMyTopics){
            getMyTopicTopicHistoryResponse(clsId, topicHistoryRequestDto.userId, pageable)
        }else{
            getAllTopicHistoryResponse(clsId, pageable)
        }


    }

    private fun getMyTopicTopicHistoryResponse(
        clsId: String,
        userId: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto> {
        val discTopicPage: Page<DiscTopic> = topicRepo.findAllByClsIdAndUserIdAndDelYn(clsId, userId, "N", pageable)
        return discTopicPage.map { it.toResponseDto() }
    }

    private fun getAllTopicHistoryResponse(
        clsId: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>{
        val discTopicPage: Page<DiscTopic> = topicRepo.findAllByClsIdAndDelYn(clsId, "N", pageable)
        return discTopicPage.map { it.toResponseDto() }
    }


}