package org.eduai.educhat.dto.member.response

data class CheckMemberResponseDto<T>(
    val status: Status,
    val data: T? = null

)

enum class Status {
    UPDATED,
    CREATED,
    UNCHANGED
}

