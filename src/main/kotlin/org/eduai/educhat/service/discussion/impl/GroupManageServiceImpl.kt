package org.eduai.educhat.service.discussion.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.annotation.PostConstruct
import org.eduai.educhat.common.enum.DiscussionRole
import org.eduai.educhat.common.enum.DiscussionStatus
import org.eduai.educhat.dto.discussion.request.CreateGroupRequestDto
import org.eduai.educhat.dto.discussion.request.DeleteDiscussionRequestDto
import org.eduai.educhat.dto.discussion.request.GetDiscussionListRequestDto
import org.eduai.educhat.dto.discussion.request.StudentListRequestDto
import org.eduai.educhat.dto.discussion.response.StudentListResponseDto
import org.eduai.educhat.entity.DiscGrp
import org.eduai.educhat.entity.DiscGrpMem
import org.eduai.educhat.repository.ClsEnrRepository
import org.eduai.educhat.repository.ClsMstRepository
import org.eduai.educhat.repository.DiscGrpMemRepository
import org.eduai.educhat.repository.DiscGrpRepository
import org.eduai.educhat.service.discussion.GroupManageService
import org.eduai.educhat.service.discussion.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.*

private data class KoreanLocaleData(val ko: KoreanData? = null)

private data class KoreanData(
    val adjectives: AdjectivesData? = null,
    val animal: AnimalData? = null
)

private data class AdjectivesData(
    val positive: List<String>? = null
)

private data class AnimalData(val name: List<String>? = null)

@Service
class GroupManageServiceImpl(
    private val grpRepo: DiscGrpRepository,
    private val grpMemRepo: DiscGrpMemRepository,
    private val clsRepo: ClsMstRepository,
    private val clsEnrRepo: ClsEnrRepository,
    private val threadManageService: ThreadManageService
) : GroupManageService {

    private val usedAnonNames = mutableSetOf<String>()

    companion object {
        private val logger = LoggerFactory.getLogger(GroupManageServiceImpl::class.java)
        private const val YAML_FILE_PATH = "word-list-ko.yaml"
    }

    private lateinit var adjectives: List<String>
    private lateinit var animals: List<String>

    @PostConstruct
    fun initializeWordLists() {
        logger.info("Initializing custom word lists from '$YAML_FILE_PATH'...")
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        try {
            val inputStream = this::class.java.classLoader.getResourceAsStream(YAML_FILE_PATH)
                ?: throw FileNotFoundException("Cannot find '$YAML_FILE_PATH' in classpath (expected in src/main/resources).")

            inputStream.use {
                val parsedData = mapper.readValue(it, KoreanLocaleData::class.java)
                adjectives = parsedData.ko?.adjectives?.positive ?: run {
                    logger.warn("'$YAML_FILE_PATH' loaded but 'positive_adjectives' list is missing or null. Using default.")
                    listOf("형용사")
                }
                animals = parsedData.ko?.animal?.name ?: run {
                    logger.warn("'$YAML_FILE_PATH' loaded but 'animal.name' list is missing or null. Using default.")
                    listOf("동물")
                }
            }
            logger.info("Successfully loaded ${adjectives.size} adjectives and ${animals.size} animals.")

        } catch (e: Exception) {
            logger.error("Failed to load or parse '$YAML_FILE_PATH': ${e.message}", e)
            adjectives = listOf("오류")
            animals = listOf("동물")
            logger.warn("Using default word lists due to loading error.")
        }
    }

    override fun getStudentList(studentListRequestDto: StudentListRequestDto): StudentListResponseDto {

        return StudentListResponseDto(
            students = clsEnrRepo.findAllUserIdAndUserNameByClsId(studentListRequestDto.clsId)
        )

    }

    override fun createGroup(createGroupRequestDto: CreateGroupRequestDto) {
        val clsId = createGroupRequestDto.clsId
        val syncTimestamp = LocalDateTime.now()
        val clsOwnerId = createGroupRequestDto.userId


        if (clsRepo.isUserOwner(clsId, clsOwnerId)) {
            createGroupRequestDto.groups.forEach { group ->
                val grpId = UUID.randomUUID()
                grpRepo.saveAndFlush(DiscGrp(
                    grpId        = grpId,
                    grpNo        = group.groupNo,
                    clsId        = clsId,
                    grpNm        = group.groupNm,
                    grpTopic     = group.topic,
                    isActive     = DiscussionStatus.ACT.toString(),
                    insDt        = syncTimestamp,
                    updDt        = syncTimestamp,
                    anonymousMode = group.isAnonymousMode
                ))
                threadManageService.createGroupChannel(clsId, grpId)

                grpMemRepo.saveAndFlush(DiscGrpMem(
                    id          = UUID.randomUUID(),
                    grpId       = grpId,
                    userId      = clsOwnerId,
                    memRole     = DiscussionRole.PROF.toString(),
                    insDt       = syncTimestamp,
                    updDt       = syncTimestamp,
                    anonymousNm = generateAnonName()
                ))

                // 멤버별 저장
                group.memberIdList.forEach { memberId ->
                    val memRepoId = UUID.randomUUID()
                    val anonName = if (group.isAnonymousMode) generateAnonName() else null

                    grpMemRepo.saveAndFlush(DiscGrpMem(
                        id           = memRepoId,
                        grpId        = grpId,
                        userId       = memberId,
                        memRole      = DiscussionRole.STUD.toString(),
                        insDt        = syncTimestamp,
                        updDt        = syncTimestamp,
                        anonymousNm  = anonName
                    ))
                }
            }
        } else {
            throw IllegalArgumentException("Not Owner for this class")
        }


    }

    override fun getDiscussList(getDiscussionListRequestDto: GetDiscussionListRequestDto): List<DiscGrp> {
        val userDiv = getDiscussionListRequestDto.userDiv
        val clsId = getDiscussionListRequestDto.clsId

        return when (userDiv) {
            //교수일 경우 전부 보여줘
            "O10" -> {
                grpRepo.findAllByClsIdAndIsActiveIn(
                    clsId,
                    listOf(DiscussionStatus.ACT.toString(), DiscussionStatus.PAU.toString())
                )
            }
            //운영자 일 경우 팅겨버려
            "O20" -> {
                throw IllegalArgumentException("invalid request")
            }
            //학생일 경우 자신이 속한 그룹만 보여줘
            else -> {
                grpRepo.findGrpListByClsIdAndUserId(clsId, getDiscussionListRequestDto.userId)
            }
        }
    }

    override fun deleteGroup(deleteGroupRequestDto: DeleteDiscussionRequestDto) {

        val clsId = deleteGroupRequestDto.clsId
        val grpId = UUID.fromString(deleteGroupRequestDto.grpId)

        val updateResult = grpRepo.updateGrpStatus(grpId, DiscussionStatus.DEL.toString(), LocalDateTime.now())
        if (updateResult == 0) {
            throw IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }
        threadManageService.removeGroupChannel(clsId, grpId)

    }

    private fun generateAnonName(): String {
        if (!::adjectives.isInitialized || !::animals.isInitialized) {
            logger.error("Word lists are not initialized. Returning default anonymous name.")
            return "익명#${(1000 until 10_000).random()}"
        }

        val adj = adjectives.randomOrNull() ?: "알수없는"
        val ani = animals.randomOrNull() ?: "동물"
        val suffix = (1000 until 10_000).random()

        val name = "$adj $ani#$suffix"

        return if (usedAnonNames.add(name)) name else generateAnonName()
    }

}