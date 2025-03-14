package org.eduai.educhat.repository

import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.eduai.educhat.entity.DiscTopic
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DiscTopicRepository: JpaRepository<DiscTopic, String> {

    fun findAllByClsIdAndUserIdAndDelYn(
        clsId: String,
        userId: String,
        delYn: String,
        pageable: Pageable
    ) : Page<DiscTopic>

    fun findAllByClsIdAndDelYn(
        clsId: String,
        delYn: String,
        pageable: Pageable
    ) : Page<DiscTopic>
}