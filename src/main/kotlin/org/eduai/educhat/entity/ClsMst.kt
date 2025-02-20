package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "cls_mst")
class ClsMst {
    @Id
    @Column(name = "cls_id", nullable = false, length = 20)
    var clsId: String? = null

    @Column(name = "subj_cd", nullable = false, length = 20)
    var subjCd: String? = null

    @Column(name = "cls_nm", nullable = false)
    var clsNm: String? = null

    @Column(name = "cls_nm_en", nullable = false)
    var clsNmEn: String? = null

    @Column(name = "cls_sec", nullable = false, length = 10)
    var clsSec: String? = null

    @Column(name = "user_id", nullable = false, length = 20)
    var userId: String? = null

    @Column(name = "cls_yr", nullable = false, length = 4)
    var clsYr: String? = null

    @Column(name = "cls_smt", nullable = false, length = 2)
    var clsSmt: String? = null

    @Column(name = "cls_grd", nullable = false, length = 2)
    var clsGrd: String? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: LocalDateTime? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: LocalDateTime? = null
}