package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscussionGrpMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionGrpMemberRepository : JpaRepository<DiscussionGrpMember, UUID> {
}