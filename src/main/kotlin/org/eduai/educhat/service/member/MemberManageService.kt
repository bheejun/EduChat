package org.eduai.educhat.service.member

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaUpdate
import jakarta.transaction.Transactional
import org.eduai.educhat.dto.member.request.CheckMemberExistRequestDto
import org.eduai.educhat.dto.member.response.CheckMemberResponseDto
import org.eduai.educhat.dto.member.response.Status
import org.eduai.educhat.entity.UserMst
import org.eduai.educhat.repository.UserMstRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@Service
class MemberManageService(
    private val userMstRepository: UserMstRepository
) {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    companion object{
        private val logger = LoggerFactory.getLogger(MemberManageService::class.java)
    }

    private inline fun  <reified T : Any> getChangedColumns(
        oldObj: T,
        newObj: T,
        ignoreFields: List<String> = listOf("id")
    ): Map<String, Pair<Any?, Any?>> {
        val differences = mutableMapOf<String, Pair<Any?, Any?>>()
        T::class.memberProperties.forEach { property ->
            if (property.name !in ignoreFields) {
                val oldValue = property.get(oldObj)
                val newValue = property.get(newObj)
                if (oldValue != newValue) {
                    differences[property.name] = oldValue to newValue
                }
            }
        }
        return differences
    }

    @Transactional
    fun checkMemExist (checkMemberExistRequestDto: CheckMemberExistRequestDto) : CheckMemberResponseDto<Any> {

        val oldUserMst = userMstRepository.findUserMstByUserId(checkMemberExistRequestDto.responseH.memId)

        val newUserMst = checkMemberExistRequestDto.responseH.toUserMst()

        //이미 정보가 등록되어 존재하는 경우
        if(oldUserMst != null){
            val differences = getChangedColumns(oldUserMst, newUserMst, ignoreFields = listOf("userId", "userPw", "phnNo", "imgPath", "insDt", "updDt"))


            //업데이트가 필요 없는 경우
            return if(differences.isEmpty()){
                logger.info(CheckMemberResponseDto(status = Status.UNCHANGED, data = null).toString())
                CheckMemberResponseDto(status = Status.UNCHANGED, data = null)
            }
            //업데이트가 필요 한 경우
            else{
                logger.info(CheckMemberResponseDto(status = Status.UPDATED, data = differences).toString())
                updateUserWithDifferences(checkMemberExistRequestDto.responseH.memId, differences)
                CheckMemberResponseDto(status = Status.UPDATED, data = differences)
            }
        }
        else{
            userMstRepository.save(
                newUserMst
            )
            logger.info(CheckMemberResponseDto(status = Status.CREATED, data = null).toString())
            return CheckMemberResponseDto(status = Status.CREATED, data = null)

        }


    }

    @Transactional
    fun updateUserWithDifferences(userId: String, differences: Map<String, Pair<Any?, Any?>>): Int {
        if (differences.isEmpty()) return 0

        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val criteriaUpdate: CriteriaUpdate<UserMst> = cb.createCriteriaUpdate(UserMst::class.java)
        val root = criteriaUpdate.from(UserMst::class.java)

        differences.forEach { (field, changePair) ->
            criteriaUpdate.set(field, changePair.second)
        }

        criteriaUpdate.set("updDt", LocalDateTime.now())
        criteriaUpdate.where(cb.equal(root.get<String>("userId"), userId))

        return entityManager.createQuery(criteriaUpdate).executeUpdate()
    }

    @Transactional
    fun checkMemExist2(userId: String) : Boolean{
        return if(userMstRepository.existsById(userId)){
            true
        }else{
            false
        }
    }






}