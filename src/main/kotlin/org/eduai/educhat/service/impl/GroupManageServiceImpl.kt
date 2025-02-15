package org.eduai.educhat.service.impl

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscussionGrp
import org.eduai.educhat.entity.DiscussionGrpMember
import org.eduai.educhat.entity.UserMst
import org.eduai.educhat.repository.DiscussionGrpMemberRepository
import org.eduai.educhat.repository.DiscussionGrpRepository
import org.eduai.educhat.repository.UserMstRepository
import org.eduai.educhat.service.GroupManageService
import org.springframework.stereotype.Service

@Service
class GroupManageServiceImpl(
    private val userMstRepository: UserMstRepository,
    private val discussionGrpRepository: DiscussionGrpRepository,
    private val discussionGrpMemberRepository: DiscussionGrpMemberRepository
) : GroupManageService {

    override fun getStudentList(): List<List<String>> {


        return userMstRepository.findAllUserIdAndUserName()
    }

    override fun createGroup(createGroupRequestDto: CreateGroupRequestDto) {
        var discussionGrp: DiscussionGrp
        var discussionGrpMember: DiscussionGrpMember
        val clsId = createGroupRequestDto.clsId

        createGroupRequestDto.groups.forEach { group ->
            discussionGrp = DiscussionGrp(
                grpNm = group.groupNm,
                grpTopic = group.topic,
                grpNo = group.groupNo,
                clsId = clsId
            )

            //그룹 저장
            discussionGrpRepository.saveAndFlush(discussionGrp)

            val memberIdList = group.memberIdList

            println(memberIdList)
            memberIdList.forEach { memberId ->
                val user : UserMst = userMstRepository.findById(memberId).orElseThrow { throw Exception("User not found") }
                discussionGrpMember = DiscussionGrpMember(
                    grp = discussionGrp,
                    role = "STUDENT",
                    user = user
                )
                //그룹 멤버 저장
                discussionGrpMemberRepository.saveAndFlush(discussionGrpMember)
            }

        }

    }

    override fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscussionGrp> {

        return discussionGrpRepository.findAllByClsId(getDiscussionListRequestDto.clsId)
    }
}