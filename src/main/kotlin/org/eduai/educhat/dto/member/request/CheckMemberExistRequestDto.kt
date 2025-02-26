package org.eduai.educhat.dto.member.request

import com.fasterxml.jackson.annotation.JsonProperty
import org.eduai.educhat.entity.UserMst
import java.time.LocalDateTime

data class CheckMemberExistRequestDto(

    @JsonProperty("JAEJ_IDNO")
    val memId : String,

    @JsonProperty("JAEJ_NAME")
    val memNm : String ? = "",

    @JsonProperty("JAEJ_DEPT")
    val memDept : String ? = "",

    @JsonProperty("JAEJ_DEPT_NAME")
    val memDeptNm : String ? = "",

    @JsonProperty("JAEJ_SOSOK")
    val memOrg : String ? = "",

    @JsonProperty("JAEJ_SOSOK_NAME")
    val memOrgNm : String ? = "",

    @JsonProperty("JAEJ_PRIV_WRITING")
    val memStatusCd : String ? = "",

    @JsonProperty("JAEJ_DIV")
    val memDiv : String ? = "",

    @JsonProperty("JAEJ_PWD")
    val memStatusMsg : String ? = "",

    @JsonProperty("ERR_MSG")
    val errMsg : String ? = ""
){

    fun toUserMst () : UserMst {
        return UserMst(
            userId = this.memId,
            userDiv = this.memDiv,
            userPw = "1234",
            userNm = this.memNm,
            dptCd = this.memDept,
            dptNm = this.memDeptNm,
            orgCd = this.memOrg,
            orgNm = this.memOrgNm,
            phnNo = null,
            imgPath = null,
            insDt = LocalDateTime.now(),
            updDt = LocalDateTime.now()
        )

    }
}
