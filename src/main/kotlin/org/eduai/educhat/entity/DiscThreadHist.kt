package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "disc_thread_hist")
class DiscThreadHist(
    @Id
    @Column(name = "msg_id", nullable = false)
    var id: UUID,

    @Column(name = "cls_id", nullable = false)
    var clsId: String,

    @Column(name = "grp_id", nullable = false)
    var grpId: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "msg", nullable = false, length = Integer.MAX_VALUE)
    var msg: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: LocalDateTime? = null

) {
}