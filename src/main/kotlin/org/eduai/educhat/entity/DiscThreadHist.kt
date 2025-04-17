package org.eduai.educhat.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "disc_thread_hist")
class DiscThreadHist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id", nullable = false)
    var id: Long = 0L,

    @Column(name = "cls_id", nullable = false)
    var clsId: String,

    @Column(name = "grp_id", nullable = false)
    var grpId: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "msg", nullable = false, length = Integer.MAX_VALUE)
    var msg: String,

    @Column(name = "user_nm", nullable = false)
    var userName: String,

    @Column(name = "ins_dt", nullable = false, updatable = false)
    var insDt: LocalDateTime = LocalDateTime.now(),  // 기본값 설정

    @Column(name = "msg_tsv", columnDefinition = "tsvector")
    var msgTsv: Any? = null
) {

}
