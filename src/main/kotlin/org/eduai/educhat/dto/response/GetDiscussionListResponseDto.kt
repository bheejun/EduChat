package org.eduai.educhat.dto.response

import org.eduai.educhat.entity.DiscGrp

data class GetDiscussionListResponseDto(
    val discussionList: List<DiscGrp>
)
