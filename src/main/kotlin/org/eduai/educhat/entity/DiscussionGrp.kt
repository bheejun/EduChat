package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
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

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt", nullable = false)
    var insDt: Instant? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: Instant? = null
) {
    @PrePersist
    fun insertDefaults() {
        insDt = Instant.now()
    }

    @PreUpdate()
    fun updateDefaults() {
        updDt = Instant.now()
    }
}