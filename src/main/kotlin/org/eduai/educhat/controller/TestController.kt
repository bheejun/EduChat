package org.eduai.educhat.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/")
    fun check0(): String {
        return "Hello"
    }

    @GetMapping("/disc/test/check")
    fun check1(): String {
        return "Hello disc"
    }

    @GetMapping("/test/check")
    fun check2(): String {
        return "Hello no disc"
    }

    @GetMapping("/disc")
    fun check3(): String {
        return "Hello a"
    }

    @GetMapping("/disc/test")
    fun check4(): String {
        return "Hello b"
    }


}