package org.eduai.educhat.dto.response

data class StudentListResponseDto(
    val students: List<StudentDto>
)

data class StudentDto(
    val userId: String,
    val userNm: String
)
