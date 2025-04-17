package org.eduai.educhat.dto.file.request

data class DownloadFilesRequestDto(
    val userId: String,
    val userPw: String,
    val fileIds: List<String>
)
