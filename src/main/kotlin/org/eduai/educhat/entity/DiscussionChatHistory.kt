package org.eduai.educhat.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.*

@Entity
@Table(name = "discussion_chat_history")
class DiscussionChatHistory(
    @Id
    @Column(name = "message_id", nullable = false)
    var id: UUID,

    @Column(name = "session_id", nullable = false)
    var sessionId: UUID,

    @Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
    var message: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_at")
    var sentAt: Instant? = null
) {
    @PrePersist
    fun insertDefaults() {
        sentAt = Instant.now()
    }
}