package org.eduai.educhat.dto.discussion.response

import org.eduai.educhat.dto.message.MessageDto

data class RestoreThreadResponseDto(
    val messages: List<MessageDto>, // 실제 메시지 객체 리스트
    val hasNext: Boolean // 다음 페이지 존재 여부
)
