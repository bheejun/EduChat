package org.eduai.educhat.dto.member.request

import org.eduai.educhat.entity.ClsMenuAuth

data class SetMenuAuthRequestDto(
    val clsId : String,
    val componentList: List<String>
){
    fun toClsMenuAuth(): ClsMenuAuth{

        return ClsMenuAuth(clsId = this.clsId, componentList = this.componentList.joinToString(","))
    }
}
