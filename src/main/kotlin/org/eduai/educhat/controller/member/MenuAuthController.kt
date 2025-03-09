package org.eduai.educhat.controller.member

import org.eduai.educhat.dto.member.request.SetMenuAuthRequestDto
import org.eduai.educhat.dto.member.response.SetMenuAuthResponseDto
import org.eduai.educhat.service.member.MenuAuthManageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/disc")
class MenuAuthController(
    private val menuAuthManageService: MenuAuthManageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MenuAuthController::class.java)
    }

    @PostMapping("/set-component")
    fun setClassMenuAuth(
    @RequestBody setMenuAuthRequestDto: SetMenuAuthRequestDto
    ): ResponseEntity<SetMenuAuthResponseDto>{

        logger.info(setMenuAuthRequestDto.toString())

        return ResponseEntity(menuAuthManageService.setMenuAuth(setMenuAuthRequestDto), HttpStatus.OK)
    }

    @GetMapping("/componentList")
    fun getComponentList(
        @RequestParam clsId : String
    ): ResponseEntity<SetMenuAuthResponseDto>{

        logger.info(menuAuthManageService.getMenuAuth(clsId).toString())

        return ResponseEntity(menuAuthManageService.getMenuAuth(clsId),HttpStatus.OK)

    }
}