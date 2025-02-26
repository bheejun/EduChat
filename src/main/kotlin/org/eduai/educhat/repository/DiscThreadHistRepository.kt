package org.eduai.educhat.repository

import org.eduai.educhat.entity.DiscThreadHist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DiscThreadHistRepository : JpaRepository<DiscThreadHist, UUID> {

    fun findTop100ByClsIdAndGrpIdOrderByInsDtDesc(clsId : String, grpId : UUID): List<DiscThreadHist>
}