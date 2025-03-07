package org.eduai.educhat.controller.member

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.eduai.educhat.dto.member.request.CheckMemberExistRequestDto
import org.eduai.educhat.dto.member.response.CheckMemberResponseDto
import org.eduai.educhat.service.member.MemberManageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/disc")
class MemberController(
    private val memberManageService: MemberManageService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(MemberController::class.java)
    }

    @PostMapping("/check")
    fun checkUserExist(
        request: HttpServletRequest,
        @RequestBody checkMemberExistRequestDto: CheckMemberExistRequestDto
    ): CheckMemberResponseDto<Any> {

        logger.info(checkMemberExistRequestDto.toString())

        val session: HttpSession = request.getSession(true)
        session.setAttribute("userId", checkMemberExistRequestDto.memId)

        logger.info("Session created: $session")

        return memberManageService.checkMemExist(checkMemberExistRequestDto)
    }

    @GetMapping("/session-check")
    fun checkSession(request: HttpServletRequest): ResponseEntity<String> {
        val session = request.getSession(false)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Not Auth")

        val username = session.getAttribute("userId") as? String
        return if (username != null) {
            ResponseEntity.ok("Valid Session, username = $username")
        } else {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Session")
        }
    }


}