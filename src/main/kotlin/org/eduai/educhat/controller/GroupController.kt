package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.request.StudentListRequestDto
import org.eduai.educhat.dto.response.GetDiscussionListResponseDto
import org.eduai.educhat.dto.response.StudentListResponseDto
import org.eduai.educhat.service.GroupManageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/discussion/group")
class GroupController(
    private val groupManageService: GroupManageService
) {
    @PostMapping("/list")
    fun getDiscussList(@RequestBody getDiscussionListRequestDto: GetDiscussionListRequestDto ) : ResponseEntity<GetDiscussionListResponseDto> {
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
        return ResponseEntity("Success", HttpStatus.OK)
    }

    @PostMapping("/delete")
    fun deleteGroup(@RequestBody request: DeleteDiscussionRequestDto): ResponseEntity<String> {
        groupManageService.deleteGroup(request)
        return ResponseEntity("Success", HttpStatus.OK)

    }

//    @PostMapping("/close")
//    fun closeGroup(@RequestBody request: DeleteDiscussionRequestDto): ResponseEntity<String> {
//        groupManageService.closeGroup(request)
//        return ResponseEntity("Success", HttpStatus.OK)
//    }

}