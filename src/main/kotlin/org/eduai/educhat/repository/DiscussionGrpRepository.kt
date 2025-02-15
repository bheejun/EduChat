package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscussionGrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscussionGrpRepository : JpaRepository<DiscussionGrp, UUID>{

    fun findAllByClsId(clsId: String): List<DiscussionGrp>

}