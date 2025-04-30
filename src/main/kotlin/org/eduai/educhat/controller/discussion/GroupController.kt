package org.eduai.educhat.controller.discussion

import org.eduai.educhat.dto.discussion.request.CreateGroupRequestDto
import org.eduai.educhat.dto.discussion.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.discussion.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.discussion.request.StudentListRequestDto
import org.eduai.educhat.dto.discussion.response.GetDiscussionListResponseDto
import org.eduai.educhat.dto.discussion.response.StudentListResponseDto
import org.eduai.educhat.service.discussion.GroupManageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/disc/group")
class GroupController(
    private val groupManageService: GroupManageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GroupController::class.java)
    }

    @PostMapping("/list")
    fun getDiscussList(@RequestBody getDiscussionListRequestDto: GetDiscussionListRequestDto) : ResponseEntity<GetDiscussionListResponseDto> {
        val response = GetDiscussionListResponseDto(
            discussionList = groupManageService.getDiscussList(getDiscussionListRequestDto)
        )
        return ResponseEntity<GetDiscussionListResponseDto>(response, HttpStatus.OK)
    }

    @PostMapping("/studList")
    fun getStudList(@RequestBody studentListRequestDto: StudentListRequestDto) : ResponseEntity<StudentListResponseDto> {
        return ResponseEntity<StudentListResponseDto>(groupManageService.getStudentList(studentListRequestDto), HttpStatus.OK)
    }

    @PostMapping("/create")
    fun createGroup(@RequestBody createGroupRequestDto: CreateGroupRequestDto) : ResponseEntity<String>{
        groupManageService.createGroup(createGroupRequestDto)
        logger.info(createGroupRequestDto.toString())
        return ResponseEntity("Success", HttpStatus.OK)
    }

    @PostMapping("/delete")
    fun deleteGroup(@RequestBody request: DeleteDiscussionRequestDto): ResponseEntity<String> {
        groupManageService.deleteGroup(request)
        return ResponseEntity("Success", HttpStatus.OK)

    }

    @PostMapping("/update")
    fun updateGroup(@RequestBody updateGrpRequestDto: CreateGroupRequestDto): ResponseEntity<String>{
        groupManageService.createGroup(updateGrpRequestDto)
        logger.info(updateGrpRequestDto.toString())
        return ResponseEntity("Success", HttpStatus.OK)
    }

}