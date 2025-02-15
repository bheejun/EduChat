package org.eduai.educhat.config

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.*

@Component
class TimeZoneConfig {

    @PostConstruct
    fun init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }

}