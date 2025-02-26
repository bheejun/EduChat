package org.eduai.educhat.dto.discussion.response

import org.eduai.educhat.entity.DiscGrp

data class GetDiscussionListResponseDto(
    val discussionList: List<DiscGrp>
)
