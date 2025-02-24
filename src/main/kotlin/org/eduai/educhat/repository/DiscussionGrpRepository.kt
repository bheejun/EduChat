package org.eduai.educhat.repository

import jakarta.transaction.Transactional
import org.eduai.educhat.entity.DiscGrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionGrpRepository : JpaRepository<DiscGrp, UUID>{

    fun findAllByClsId(clsId: String): List<DiscGrp>

    @Modifying
    @Transactional
    @Query("" +
            "update disc_grp " +
            "set is_active = :isActive " +
            "where grp_id = :grpId",
        nativeQuery = true)
    fun updateGrpStatus(grpId: UUID, isActive: String) :Int

    fun findAllByClsIdAndIsActive(clsId: String, isActive: String): List<DiscGrp>


    @Query("""
        select * 
        from disc_grp as DG
        where DG.cls_id = :clsId
        and DG.is_active = 'ACT'
        AND EXISTS (
            select 1 
            from disc_grp_mem as DGM
            where DGM.grp_id = DG.grp_id
            AND DGM.user_id = :userId
        )
    """ , nativeQuery = true)
    fun findGrpListByClsIdAndUserId(clsId: String, userId: String): List<DiscGrp>


}