package org.eduai.educhat.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "disc_thread_hist")
class DiscThreadHist(
    @Id
    @Column(name = "msg_id", nullable = false)
    var id: UUID,

    @Column(name = "session_id", nullable = false)
    var sessionId: UUID,

    @Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
    var message: String,


    @Column(name = "grp_id", nullable = false)
    var grpId: UUID? = null,

    @Column(name = "msg", nullable = false, length = Integer.MAX_VALUE)
    var msg: String? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: Instant? = null

) {
}