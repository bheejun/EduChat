package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "discussion_grp_member")
class DiscussionGrpMember(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grp_id", nullable = false)
    var grp: DiscussionGrp? = null,

    @Column(name = "role", length = 50)
    var role: String? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: Instant? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserMst? = null
) {
    @PrePersist
    fun insertDefaults() {
        insDt = Instant.now()
    }

    @PreUpdate()
    fun updateDefaults() {
        updDt = Instant.now()
    }

    constructor() : this(null, null, null, null, null, null)
}