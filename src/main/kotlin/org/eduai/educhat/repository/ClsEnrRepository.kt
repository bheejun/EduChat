package org.eduai.educhat.repository

import org.eduai.educhat.dto.response.StudentDto
import org.eduai.educhat.entity.ClsEnr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface ClsEnrRepository : JpaRepository<ClsEnr, String>{

    @Query("""
        Select CE.user_id, um.user_nm
        from cls_enr CE
        inner join user_mst UM
        on CE.user_id = UM.user_id 
        where CE.cls_id = :clsId
        AND UM.user_div = 'S'
    """, nativeQuery = true)
    fun findAllUserIdAndUserNameByClsId(clsId : String): List<StudentDto>

}