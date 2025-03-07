package org.eduai.educhat.service.member

import org.eduai.educhat.dto.member.request.SetMenuAuthRequestDto
import org.eduai.educhat.dto.member.response.SetMenuAuthResponseDto
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
        val menuAuth = clsMenuAuthRepository.findById(clsId)
            .orElse(null) ?: return SetMenuAuthResponseDto()  // 값이 없으면 빈 DTO 반환

        return SetMenuAuthResponseDto(menuAuth.componentList?.split(","))
    }


}