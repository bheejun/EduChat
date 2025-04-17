package org.eduai.educhat.dto.discussion.request

data class AddTestMessagesRequestDto(
    val clsId: String,        // 대상 클래스 ID
    val grpId: String,        // 대상 그룹 ID (UUID 문자열)
    val count: Int = 100,     // 생성할 메시지 개수 (기본값 100)
    val senderId: String,     // 테스트 메시지 발신자 ID
    val senderName: String    // 테스트 메시지 발신자 이름
)
