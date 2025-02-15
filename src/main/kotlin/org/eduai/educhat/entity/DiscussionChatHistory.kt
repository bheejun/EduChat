package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.*

@Entity
@Table(name = "discussion_chat_history")
class DiscussionChatHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "message_id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    var session: DiscussionChatSession? = null,

    @Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
    var message: String? = null,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_at")
    var sentAt: Instant? = null
) {
    @PrePersist
    fun insertDefaults() {
        sentAt = Instant.now()
    }

        constructor() : this(null, null, null, null)
}