package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "comm_web_log")
class CommWebLog(

    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "web_log_id", nullable = false)
    var id: UUID? = null,

    @Column(name = "web_log_date", length = 8)
    var webLogDate: String? = null,

    @Column(name = "web_log_url", length = Integer.MAX_VALUE)
    var webLogUrl: String? = null,

    @Column(name = "user_id", length = 20)
    var userId: String? = null,

    @Column(name = "user_ip", length = 20)
    var userIp: String? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt", nullable = false)
    var insDt: LocalDateTime? = null

) {
}