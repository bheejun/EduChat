package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.CreateGroupRequestDto
import org.eduai.educhat.dto.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.response.GetDiscussionListResponseDto
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

    @GetMapping("/studList")
    fun getStudList() : ResponseEntity<List<List<String>>> {
        return ResponseEntity<List<List<String>>>(groupManageService.getStudentList(), HttpStatus.OK)
    }

    @PostMapping("/create")
    fun createGroup(@RequestBody createGroupRequestDto: CreateGroupRequestDto) : ResponseEntity<List<String>>{

        groupManageService.createGroup(createGroupRequestDto)
        return ResponseEntity(listOf("group1", "group2"), HttpStatus.OK)
    }
}