package org.eduai.educhat.repository

import jakarta.transaction.Transactional
import org.eduai.educhat.entity.DiscussionGrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionGrpRepository : JpaRepository<DiscussionGrp, UUID>{

    fun findAllByClsId(clsId: String): List<DiscussionGrp>

    @Modifying
    @Transactional
    @Query("" +
            "update discussion_grp " +
            "set is_active = :isActive " +
            "where grp_id = :grpId",
        nativeQuery = true)
    fun updateGrpStatus(grpId: UUID, isActive: String) :Int

    fun findAllByClsIdAndIsActive(clsId: String, isActive: String): List<DiscussionGrp>

}