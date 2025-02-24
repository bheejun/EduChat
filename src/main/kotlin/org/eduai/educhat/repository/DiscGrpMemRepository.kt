package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscGrpMem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscGrpMemRepository : JpaRepository<DiscGrpMem, UUID> {

    @Query("SELECT EXISTS( " +
            "SELECT 1 " +
            "FROM disc_grp_mem " +
            "WHERE user_id = ?1 " +
            "AND grp_id = ?2)  " +
            "", nativeQuery = true)
    fun findGrpMemByUserId(userId: String, grpId: UUID) : Boolean?


    fun findGrpMemByUserIdAndGrpId(userId: String, grpId: UUID) : DiscGrpMem?

}