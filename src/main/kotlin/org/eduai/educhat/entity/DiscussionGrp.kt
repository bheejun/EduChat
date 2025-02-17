package org.eduai.educhat.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "discussion_grp")
class DiscussionGrp(
    @Id
    @Column(name = "grp_id", nullable = false)
    var grpId: UUID,

    @Column(name = "grp_no", nullable = false)
    var grpNo: Int,

    @Column(name = "cls_id", nullable = false, length = 20)
    var clsId: String,

    @Column(name = "grp_topic")
    var grpTopic: String,

    @Column(name = "grp_nm")
    var grpNm: String,

    @Column(name = "is_active")
    var isActive: String,


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "ins_dt", nullable = false)
    var insDt: LocalDateTime,


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "upd_dt", nullable = false)
    var updDt: LocalDateTime
) {
}