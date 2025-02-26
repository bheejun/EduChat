package org.eduai.educhat.controller

import org.eduai.educhat.dto.InsertLogRequestDto
import org.eduai.educhat.entity.CommWebLog
import org.eduai.educhat.repository.CommWebLogRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@RestController
@RequestMapping("/disc/log")
class LogController(
    private val commWebLogRepository: CommWebLogRepository
) {


    @PostMapping("/insert")
    fun insertWebLog(@RequestBody insertLogRequestDto : InsertLogRequestDto) :ResponseEntity<String> {
        commWebLogRepository.save( CommWebLog(
            id = UUID.randomUUID(),
            webLogUrl = insertLogRequestDto.url,
            userId = insertLogRequestDto.userId,
            userIp = insertLogRequestDto.userIp,
            webLogDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
            insDt = LocalDateTime.now()
        ) )

        return ResponseEntity("Success", HttpStatus.OK)
    }


}