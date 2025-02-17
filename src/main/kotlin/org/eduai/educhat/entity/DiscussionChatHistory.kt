package org.eduai.educhat.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDateTime
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "sent_at")
    var sentAt: LocalDateTime
) {
}