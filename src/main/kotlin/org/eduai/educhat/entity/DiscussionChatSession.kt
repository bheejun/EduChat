package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.*

@Entity
@Table(name = "discussion_chat_session")
class DiscussionChatSession(
    @Id
    @Column(name = "discussion_session_id", nullable = false)
    var discussionSessionId: UUID,

    @Column(name = "grp_id", nullable = false)
    var grpId: UUID,

    @Column(name = "is_active", length = 10)
    var isActive: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
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