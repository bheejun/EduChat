package org.eduai.educhat.service.discussion

import org.eduai.educhat.common.enum.DiscussionStatus
import org.eduai.educhat.dto.discussion.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscGrp
import org.eduai.educhat.repository.DiscGrpRepository
import org.springframework.stereotype.Service

@Service
class DiscussionHistoryService(
    private val grpRepo : DiscGrpRepository
) {

    fun getDiscHistList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscGrp> {
        val userDiv = getDiscussionListRequestDto.userDiv
        val clsId = getDiscussionListRequestDto.clsId

        return when (userDiv) {
            //교수일 경우 전부 보여줘
            "O10" -> {
                grpRepo.findAllByClsIdAndIsActiveIn(
                    clsId,
                    listOf(DiscussionStatus.FIN.toString())
                )
            }
            //운영자 일 경우 팅겨버려
            "O20" -> {
                throw IllegalArgumentException("invalid request")
            }
            //학생일 경우 자신이 속한 그룹만 보여줘
            else -> {
                grpRepo.findGrpHistListByClsIdAndUserId(clsId, getDiscussionListRequestDto.userId)
            }
        }
    }
}