package org.eduai.educhat.dto.discussion.request

data class CreateGroupRequestDto(

    val groups: List<GroupDto>,
    val clsId : String,
    val userId : String
)

data class GroupDto(
    val groupNo: Int,
    val memberIdList: List<String>,
    val topic: String,
    val groupNm : String,
    val isAnonymousMode: Boolean
)