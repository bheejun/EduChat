package org.eduai.educhat.dto.response

import org.eduai.educhat.entity.DiscussionGrp

data class GetDiscussionListResponseDto(
    val discussionList: List<DiscussionGrp>
)
