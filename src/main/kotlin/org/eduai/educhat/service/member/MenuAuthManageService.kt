package org.eduai.educhat.service.member

import org.eduai.educhat.dto.member.request.SetMenuAuthRequestDto
import org.eduai.educhat.dto.member.response.SetMenuAuthResponseDto
import org.eduai.educhat.entity.ClsMenuAuth
import org.eduai.educhat.repository.ClsMenuAuthRepository
import org.springframework.stereotype.Service

@Service
class MenuAuthManageService(
    private val clsMenuAuthRepository: ClsMenuAuthRepository
) {

    fun setMenuAuth(setMenuAuthRequestDto: SetMenuAuthRequestDto): SetMenuAuthResponseDto{

        clsMenuAuthRepository.save(setMenuAuthRequestDto.toClsMenuAuth())

        return SetMenuAuthResponseDto(setMenuAuthRequestDto.componentList)

    }

    fun getMenuAuth(clsId: String): SetMenuAuthResponseDto {
        val menuAuthOpt = clsMenuAuthRepository.findById(clsId)
        return if (menuAuthOpt.isPresent) {
            val menuAuth = menuAuthOpt.get()
            SetMenuAuthResponseDto(menuAuth.componentList?.split(","))
        } else {
            // 기본 허용 메뉴 리스트
            val defaultMenus = listOf("/review", "/preview", "/schat", "/topicextraction", "/sdiscussion")
            val defaultComponentList = defaultMenus.joinToString(",")
            // 새로운 ClsMenuAuth 객체 생성 (필요한 필드에 맞게 수정)
            val newMenuAuth = ClsMenuAuth(
                clsId = clsId,
                componentList = defaultComponentList
            )
            // DB에 저장
            clsMenuAuthRepository.save(newMenuAuth)
            SetMenuAuthResponseDto(defaultMenus)
        }
    }


}