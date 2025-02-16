package org.eduai.educhat.service.impl

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscussionGrp
import org.eduai.educhat.entity.DiscussionGrpMember
import org.eduai.educhat.repository.DiscussionGrpMemberRepository
import org.eduai.educhat.repository.DiscussionGrpRepository
import org.eduai.educhat.repository.UserMstRepository
import org.eduai.educhat.service.GroupManageService
import org.eduai.educhat.service.ThreadManageService
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupManageServiceImpl(
    private val threadManageService: ThreadManageService,
    private val userMstRepository: UserMstRepository,
    private val grpRepo: DiscussionGrpRepository,
    private val grpMemRepo: DiscussionGrpMemberRepository
) : GroupManageService {

    override fun getStudentList(): List<List<String>> {


        return userMstRepository.findAllUserIdAndUserName()
    }

    override fun createGroup(createGroupRequestDto: CreateGroupRequestDto) {
        val clsId = createGroupRequestDto.clsId
        val grpId = UUID.randomUUID()

        createGroupRequestDto.groups.forEach { group ->
            grpRepo.saveAndFlush(DiscussionGrp(
                grpId = grpId,
                grpNo = group.groupNo,
                clsId = clsId,
                grpNm = group.groupNm,
                grpTopic = group.topic
            ))

            threadManageService.createGroupChannel(clsId, grpId)

            val memberIdList = group.memberIdList

            memberIdList.forEach { memberId ->
                //그룹 멤버 저장
                val memRepoId = UUID.randomUUID()
                grpMemRepo.saveAndFlush(DiscussionGrpMember(
                    id = memRepoId,
                    grpId = grpId,
                    userId = memberId,
                    role = "STUD"
                ))
            }

        }

    }

    override fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscussionGrp> {

        return grpRepo.findAllByClsId(getDiscussionListRequestDto.clsId)
    }
}