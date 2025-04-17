package org.eduai.educhat.dto.discussion.response

import org.eduai.educhat.dto.message.SearchResultMessageDto

data class SearchResponseDto(
    val msgResult: List<SearchResultMessageDto>,
    val userResult: List<SearchResultMessageDto>
)
