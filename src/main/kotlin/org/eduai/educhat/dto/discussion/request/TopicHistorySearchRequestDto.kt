package org.eduai.educhat.dto.discussion.request

data class TopicHistorySearchRequestDto(
    val clsId: String,
    val userId : String,
    val searchTerm: String,
    val pageSize : Int,
    val pageNum : Int,
    val searchOption: String,
    val onlyMyTopics : Boolean
)
