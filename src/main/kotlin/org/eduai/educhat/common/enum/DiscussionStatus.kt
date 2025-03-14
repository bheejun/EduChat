package org.eduai.educhat.common.enum

enum class DiscussionStatus(val description: String) {
    ACT("진행 중"),
    PAU("일시 정지"),
    FIN("종료됨"),
    DEL("삭제 됨"),
    LCK("전체 채팅제한")

}