package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscussionChatSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionChatSessionRepository : JpaRepository<DiscussionChatSession, UUID> {
    fun findByGrpId(grpId: UUID): DiscussionChatSession?


    fun updateStatusByGrpId(grpId: UUID, status: String) : Int
}