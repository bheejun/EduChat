package org.eduai.educhat.service

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.request.StudentListRequestDto
import org.eduai.educhat.dto.response.StudentListResponseDto
import org.eduai.educhat.entity.DiscGrp


interface GroupManageService{

    fun getStudentList(studentListRequestDto: StudentListRequestDto): StudentListResponseDto

    fun createGroup(createGroupRequestDto: CreateGroupRequestDto)

    fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscGrp>

    fun deleteGroup(deleteGroupRequestDto: DeleteDiscussionRequestDto)

}