package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "user_mst")
class UserMst {
    @Id
    @Column(name = "user_id", nullable = false, length = 20)
    var userId: String? = null

    @Column(name = "user_div", nullable = false, length = 20)
    var userDiv: String? = null

    @Column(name = "user_pw", nullable = false)
    var userPw: String? = null

    @Column(name = "user_nm", nullable = false, length = 100)
    var userNm: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "dpt_cd", length = 10)
    var dptCd: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "dpt_nm", length = 100)
    var dptNm: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "org_cd", length = 10)
    var orgCd: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "org_nm", length = 100)
    var orgNm: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "phn_no", length = 20)
    var phnNo: String? = null

    @ColumnDefault("NULL::character varying")
    @Column(name = "img_path")
    var imgPath: String? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: Instant? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: Instant? = null
}