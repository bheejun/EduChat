package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscThreadHist
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface DiscThreadHistRepository : JpaRepository<DiscThreadHist, UUID> {

    fun findTop100ByClsIdAndGrpIdOrderByInsDtDesc(clsId : String, grpId : UUID): List<DiscThreadHist>


    fun findByClsIdAndGrpIdAndInsDtBeforeOrderByInsDtDesc(
        clsId: String,
        grpId: UUID,
        insDt: LocalDateTime,
        pageable: Pageable
    ): Page<DiscThreadHist>

    fun findByClsIdAndGrpIdOrderByInsDtDesc(
        clsId: String,
        grpId: UUID,
        pageable: Pageable
    ): Page<DiscThreadHist>


    @Query(
        value = """
        SELECT *
        FROM disc_thread_hist
        WHERE msg ILIKE CONCAT('%', :searchTerm, '%')
            AND grp_id = :grpId
        order by ins_dt desc;
      """,
        nativeQuery = true
    )
    fun searchMsgBySearchTerm(grpId: UUID, searchTerm: String): List<DiscThreadHist>

    @Query(
        value = """
        SELECT *
        FROM disc_thread_hist
        WHERE disc_thread_hist.user_nm ILIKE CONCAT('%', :searchTerm, '%')
            AND grp_id = :grpId
        order by ins_dt desc;
      """,
        nativeQuery = true
    )
    fun searchUserBySearchTerm(grpId: UUID, searchTerm: String) : MutableList<DiscThreadHist>

    @Query("SELECT dth.id FROM DiscThreadHist dth WHERE dth.id IN :msgIds")
    fun findIdsByMsgIds(msgIds: List<UUID>): List<UUID>
}