package org.eduai.educhat.repository

import jakarta.transaction.Transactional
import org.eduai.educhat.entity.DiscGrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface DiscGrpRepository : JpaRepository<DiscGrp, UUID>{

    fun findAllByClsId(clsId: String): List<DiscGrp>

    @Modifying
    @Transactional
    @Query("" +
            "update disc_grp " +
            "set is_active = :isActive, upd_dt =:updDt " +
            "where grp_id = :grpId",
        nativeQuery = true)
    fun updateGrpStatus(grpId: UUID, isActive: String, updDt: LocalDateTime) :Int

    fun findAllByClsIdAndIsActiveIn(clsId: String, isActive: Collection<String>): List<DiscGrp>


    @Query("""
        select * 
        from disc_grp as DG
        where DG.cls_id = :clsId
        and (DG.is_active = 'ACT' or DG.is_active = 'PAU')
        AND EXISTS (
            select 1 
            from disc_grp_mem as DGM
            where DGM.grp_id = DG.grp_id
            AND DGM.user_id = :userId
        )
    """ , nativeQuery = true)
    fun findGrpListByClsIdAndUserId(clsId: String, userId: String): List<DiscGrp>

    fun findByGrpId(grpId: UUID) : DiscGrp


}