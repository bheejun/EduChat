package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscussionGrpMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionGrpMemberRepository : JpaRepository<DiscussionGrpMember, UUID> {

    @Query("SELECT EXISTS( " +
            "SELECT 1 " +
            "FROM discussion_grp_member " +
            "WHERE user_id = ?1 " +
            "AND grp_id = ?2)  " +
            "", nativeQuery = true)
    fun findGrpMemByUserId(userId: String, grpId: UUID) : Boolean?

}