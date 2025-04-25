package org.eduai.educhat.service.discussion

import org.eduai.educhat.dto.discussion.request.TopicHistoryRequestDto
import org.eduai.educhat.dto.discussion.request.TopicHistorySearchRequestDto
import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.eduai.educhat.repository.DiscTopicRepository
import org.eduai.educhat.service.discussion.impl.ThreadManageServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class TopicManageService(
    private val topicRepo : DiscTopicRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TopicManageService::class.java)
    }

    fun getTopicHistoryList(topicHistoryRequestDto: TopicHistoryRequestDto) : Page<TopicHistoryResponseDto> {

        val pageable = PageRequest.of(topicHistoryRequestDto.pageNum, topicHistoryRequestDto.pageSize)
        val clsId = topicHistoryRequestDto.clsId
        val userId = topicHistoryRequestDto.userId

        return if(topicHistoryRequestDto.onlyMyTopics){
            topicRepo.selectAllMyTopicList(clsId, userId, pageable)
        }else{
            topicRepo.selectAllTopicList(clsId, pageable)
        }


    }

    fun searchTopicHistoryByOption(searchRequestDto: TopicHistorySearchRequestDto): Page<TopicHistoryResponseDto>{
        val pageable = PageRequest.of(searchRequestDto.pageNum, searchRequestDto.pageSize)
        val clsId = searchRequestDto.clsId
        val searchOption = searchRequestDto.searchOption
        val searchTerm = searchRequestDto.searchTerm
        logger.info(searchRequestDto.toString())
        when (searchOption) {
            "subject" -> {
                return topicRepo.searchSubject(clsId, searchTerm, pageable)
            }

            "content" -> {
                return topicRepo.searchContent(clsId, searchTerm, pageable)
            }

            "user" -> {
                return topicRepo.searchUser(clsId, searchTerm, pageable)
            }

            else -> {
                throw IllegalArgumentException("Search option is not valid")
            }
        }


    }


}