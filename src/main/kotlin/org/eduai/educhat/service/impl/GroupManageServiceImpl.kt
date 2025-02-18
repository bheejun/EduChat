package org.eduai.educhat.service.impl

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscussionGrp
import org.eduai.educhat.entity.DiscussionGrpMember
import org.eduai.educhat.repository.DiscussionGrpMemberRepository
import org.eduai.educhat.repository.DiscussionGrpRepository
import org.eduai.educhat.repository.UserMstRepository
import org.eduai.educhat.service.GroupManageService
import org.eduai.educhat.service.ThreadManageService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class GroupManageServiceImpl(
    private val userMstRepository: UserMstRepository,
    private val grpRepo: DiscussionGrpRepository,
    private val grpMemRepo: DiscussionGrpMemberRepository,
    private val threadManageService: ThreadManageService
) : GroupManageService {

    override fun getStudentList(): List<List<String>> {


        return userMstRepository.findAllUserIdAndUserName()
    }

    override fun createGroup(createGroupRequestDto: CreateGroupRequestDto) {
        val clsId = createGroupRequestDto.clsId

        //인서트 시간 동기화
        val syncTimestamp = LocalDateTime.now()

        createGroupRequestDto.groups.forEach { group ->
            //그룹별 아이디 부여
            val grpId = UUID.randomUUID()

            grpRepo.saveAndFlush(DiscussionGrp(
                grpId = grpId,
                grpNo = group.groupNo,
                clsId = clsId,
                grpNm = group.groupNm,
                grpTopic = group.topic,
                isActive = "ACT",
                insDt = syncTimestamp,
                updDt = syncTimestamp
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
                    role = "STUD",
                    insDt = syncTimestamp,
                    updDt = syncTimestamp
                ))
            }

        }

    }

    override fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscussionGrp> {

        return grpRepo.findAllByClsIdAndIsActive(getDiscussionListRequestDto.clsId, "ACT")
    }

    override fun deleteGroup(deleteGroupRequestDto: DeleteDiscussionRequestDto) {

        val clsId = deleteGroupRequestDto.clsId
        val grpId = UUID.fromString(deleteGroupRequestDto.grpId)

        val updateResult = grpRepo.updateGrpStatus(grpId, "DEL")
        if (updateResult == 0) {
            throw IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }
        threadManageService.removeGroupChannel(clsId, grpId)

    }
}