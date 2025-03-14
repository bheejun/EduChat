package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "disc_topic")
class DiscTopic(
    @Id
    @Column(name = "session_id", nullable = false, length = 50)
    var sessionId: String,

    @Column(name = "user_id", nullable = false, length = 20)
    var userId: String,

    @Column(name = "cls_id", nullable = false, length = 20)
    var clsId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "topic_content", nullable = false)
    var topicContent: MutableMap<String, Any>? = null,

    @ColumnDefault("'N'::bpchar")
    @Column(name = "del_yn", nullable = false, length = Integer.MAX_VALUE)
    var delYn: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ins_dt")
    var insDt: LocalDateTime,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "upd_dt")
    var updDt: LocalDateTime

) {

    fun toResponseDto(): TopicHistoryResponseDto {
        return TopicHistoryResponseDto(
            sessionId = this.sessionId,
            userId = this.userId ,
            clsId = this.clsId ,
            topicContent = this.topicContent ?: mutableMapOf(),
            insDt = this.insDt
        )
    }

}