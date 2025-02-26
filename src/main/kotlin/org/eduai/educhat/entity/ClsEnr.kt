package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "cls_enr")
class ClsEnr(

    @Id
    @Column(name = "cls_enr_id", nullable = false, length = 50)
    var clsEnrId: String,

    @Column(name = "user_id", nullable = false, length = 20)
    var userId: String,

    @Column(name = "cls_id", nullable = false, length = 20)
    var clsId: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: LocalDateTime? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: LocalDateTime? = null

) {
}