package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.*

@Entity
@Table(name = "discussion_grp")
class DiscussionGrp(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "grp_id", nullable = false)
    var id: UUID? = null,

    @Column(name = "grp_no", nullable = false)
    var grpNo: Int? = null,

    @Column(name = "cls_id", nullable = false, length = 20)
    var clsId: String? = null,

    @Column(name = "grp_topic")
    var grpTopic: String? = null,

    @Column(name = "grp_nm")
    var grpNm: String? = null,

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

        constructor() : this(null, null, null, null, null, null, null)
}