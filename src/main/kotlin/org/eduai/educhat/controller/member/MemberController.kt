package org.eduai.educhat.controller.member

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.eduai.educhat.dto.member.request.CheckMemberExistRequestDto
import org.eduai.educhat.dto.member.response.CheckMemberResponseDto
import org.eduai.educhat.dto.member.response.Status
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

    @PostMapping("/check/hallym")
    fun checkUserExist(
        request: HttpServletRequest,
        @RequestBody checkMemberExistRequestDto: CheckMemberExistRequestDto
    ): CheckMemberResponseDto<Any> {

        logger.info(checkMemberExistRequestDto.toString())
        val checkRequest = checkMemberExistRequestDto.responseH

        val session: HttpSession = request.getSession(true)
        session.setAttribute("userId", checkRequest.memId)
        session.setAttribute("userNm", checkRequest.memNm)
        session.setAttribute("userDiv", checkRequest.memDiv)
        session.setAttribute("userPw", checkMemberExistRequestDto.userPw)

        logger.info("Session created: $session")

        return memberManageService.checkMemExist(checkMemberExistRequestDto)
    }

    @GetMapping("/check/normal")
    fun checkUserExist2(
        request: HttpServletRequest,
        @RequestParam userId : String
    ): CheckMemberResponseDto<Any> {
        if(memberManageService.checkMemExist2(userId)){
            logger.info(userId + "입장")
            val session: HttpSession = request.getSession(true)
            session.setAttribute("userId", userId)

            logger.info("Session created: $session")

            return CheckMemberResponseDto(status = Status.UNCHANGED, data = null)
        }else{
            throw IllegalArgumentException("Not Exist User")
        }
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

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {
        val session = request.getSession(false)
        session?.invalidate()

        val cookie = Cookie("JSESSIONID", null)
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)

        return ResponseEntity.ok("Logged out successfully")
    }


}