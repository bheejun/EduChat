package org.eduai.educhat.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "disc_grp_mem")
class DiscGrpMem(
    @Id
    @Column(name = "mem_id", nullable = false)
    var id: UUID,

    @Column(name = "grp_id", nullable = false)
    var grpId: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "mem_role", length = 50)
    var memRole: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "ins_dt", nullable = false)
    var insDt: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "upd_dt")
    var updDt: LocalDateTime,

    @Column(name = "anonymous_nm", nullable = true)
    var anonymousNm: String ? = ""
) {
}