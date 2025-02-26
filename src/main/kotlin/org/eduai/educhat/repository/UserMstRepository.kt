package org.eduai.educhat.repository

import org.eduai.educhat.entity.UserMst
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserMstRepository : JpaRepository<UserMst, String> {

    @Query("Select user_id, user_nm From user_mst", nativeQuery = true)
    fun findAllUserIdAndUserNameByClsId(clsId : String): List<List<String>>

}