package org.eduai.educhat.dto.discussion.request

data class TopicHistoryRequestDto(
    val clsId : String,
    val userId : String,
    val pageSize : Int,
    val pageNum : Int,
    val onlyMyTopics : Boolean
)
