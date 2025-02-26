package org.eduai.educhat.service.discussion

import org.eduai.educhat.dto.discussion.request.CreateGroupRequestDto
import org.eduai.educhat.dto.discussion.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.discussion.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.discussion.request.StudentListRequestDto
import org.eduai.educhat.dto.discussion.response.StudentListResponseDto
import org.eduai.educhat.entity.DiscGrp


interface GroupManageService{

    fun getStudentList(studentListRequestDto: StudentListRequestDto): StudentListResponseDto

    fun createGroup(createGroupRequestDto: CreateGroupRequestDto)

    fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscGrp>

    fun deleteGroup(deleteGroupRequestDto: DeleteDiscussionRequestDto)

}