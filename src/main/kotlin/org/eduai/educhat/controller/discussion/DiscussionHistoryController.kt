package org.eduai.educhat.controller.discussion

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/disc/history")
class DiscussionHistoryController {

    @PostMapping("/list")
    fun getDiscHistoryList(){

    }

    @PostMapping("/detail")
    fun getDiscHistoryDetail(){

    }

    @PostMapping("/stats")
    fun getDiscHistoryStats(){

    }

}