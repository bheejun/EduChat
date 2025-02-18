package org.eduai.educhat.service

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.entity.DiscussionGrp


interface GroupManageService{

    fun getStudentList(): List<List<String>>

    fun createGroup(createGroupRequestDto: CreateGroupRequestDto)

    fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscussionGrp>

    fun deleteGroup(deleteGroupRequestDto: DeleteDiscussionRequestDto)

}