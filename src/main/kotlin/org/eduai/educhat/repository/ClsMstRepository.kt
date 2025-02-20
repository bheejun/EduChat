package org.eduai.educhat.repository

import org.eduai.educhat.entity.ClsMst
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ClsMstRepository : JpaRepository<ClsMst, String> {

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM cls_mst
            WHERE cls_id = ?1
            AND user_id = ?2
        )
    """, nativeQuery = true)
    fun isUserOwner(clsId: String, userId: String): Boolean
}