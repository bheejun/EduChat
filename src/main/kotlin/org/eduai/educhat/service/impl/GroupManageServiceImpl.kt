package org.eduai.educhat.service.impl

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscGrp
import org.eduai.educhat.entity.DiscGrpMem
import org.eduai.educhat.repository.DiscGrpMemRepository
import org.eduai.educhat.repository.DiscGrpRepository
import org.eduai.educhat.repository.UserMstRepository
import org.eduai.educhat.service.GroupManageService
import org.eduai.educhat.service.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class GroupManageServiceImpl(
    private val userMstRepository: UserMstRepository,
    private val grpRepo: DiscGrpRepository,
    private val grpMemRepo: DiscGrpMemRepository,
    private val threadManageService: ThreadManageService
) : GroupManageService {

    companion object {
        private val logger = LoggerFactory.getLogger(GroupManageServiceImpl::class.java)
    }

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

            grpRepo.saveAndFlush(DiscGrp(
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
                grpMemRepo.saveAndFlush(DiscGrpMem(
                    id = memRepoId,
                    grpId = grpId,
                    userId = memberId,
                    memRole = "STUD",
                    insDt = syncTimestamp,
                    updDt = syncTimestamp
                ))
            }

        }

    }

    override fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscGrp> {
        val userDiv = getDiscussionListRequestDto.userDiv
        val clsId = getDiscussionListRequestDto.clsId

        return when (userDiv) {
            //교수일 경우 전부 보여줘
            "O10" -> {
                grpRepo.findAllByClsIdAndIsActive(clsId, "ACT")
            }
            //운영자 일 경우 팅겨버려
            "O20" -> {
                throw IllegalArgumentException("invalid request")
            }
            //학생일 경우 자신이 속한 그룹만 보여줘
            else -> {
                grpRepo.findGrpListByClsIdAndUserId(clsId, getDiscussionListRequestDto.userId)
            }
        }
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