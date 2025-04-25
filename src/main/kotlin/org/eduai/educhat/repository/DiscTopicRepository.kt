package org.eduai.educhat.repository

import org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto
import org.eduai.educhat.entity.DiscTopic
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DiscTopicRepository : JpaRepository<DiscTopic, String> {

    @Query(
        value = """
            SELECT new org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto(
                t.sessionId,
                u.userNm,
                t.clsId,
                t.topicContent,
                t.insDt
            )
            FROM DiscTopic t
            LEFT JOIN UserMst u
                ON t.userId = u.userId
            WHERE t.clsId    = :clsId
                AND t.userId   = :userId
                AND t.delYn    = 'N'
            ORDER BY t.insDt DESC
        """,
        countQuery = """
            SELECT count(t)
            FROM DiscTopic t
            WHERE t.clsId  = :clsId
                AND t.userId = :userId
                AND t.delYn  = 'N'
        """
    )
    fun selectAllMyTopicList(
        clsId: String,
        userId: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>

    @Query(
        value = """
            SELECT new org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto(
                t.sessionId,
                u.userNm,
                t.clsId,
                t.topicContent,
                t.insDt
            )
            FROM DiscTopic t
            LEFT JOIN UserMst u
                ON t.userId = u.userId
            WHERE t.clsId    = :clsId
                AND t.delYn    = 'N'
            ORDER BY t.insDt DESC
        """,
        countQuery = """
            SELECT count(t)
            FROM DiscTopic t
            WHERE t.clsId  = :clsId
                AND t.delYn  = 'N'
        """
    )
    fun selectAllTopicList(
        clsId: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>

    @Query(
        value = """
        SELECT new org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto(
            t.sessionId,
            u.userNm,
            t.clsId,
            t.topicContent,
            t.insDt
        )
        FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId
        WHERE t.clsId = :clsId
          AND t.delYn = 'N'
          AND LOWER( function('jsonb_extract_path_text', t.topicContent, '핵심 주제') ) 
              LIKE LOWER( CONCAT('%', :searchTerm, '%') )
        ORDER BY t.insDt DESC
    """,
        countQuery = """
        SELECT count(t)
        FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId
        WHERE t.clsId = :clsId
          AND t.delYn = 'N'
          AND LOWER( function('jsonb_extract_path_text', t.topicContent, '핵심 주제') ) 
              LIKE LOWER( CONCAT('%', :searchTerm, '%') )
    """
    )
    fun searchSubject(
        @Param("clsId") clsId: String,
        @Param("searchTerm") searchTerm: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>


    // 2) 토론 내용(리스트) 검색 (JPQL + function 사용)
    @Query(
        value = """
        SELECT new org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto(
            t.sessionId,
            u.userNm,
            t.clsId,
            t.topicContent,
            t.insDt
        )
        FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId
        WHERE t.clsId = :clsId
          AND t.delYn = 'N'
          AND LOWER( function('jsonb_extract_path_text', t.topicContent, '토론 주제 리스트') ) 
              LIKE LOWER( CONCAT('%', :searchTerm, '%') )
        ORDER BY t.insDt DESC
    """,
        countQuery = """
        SELECT count(t)
        FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId
        WHERE t.clsId = :clsId
          AND t.delYn = 'N'
          AND LOWER( function('jsonb_extract_path_text', t.topicContent, '토론 주제 리스트') ) 
              LIKE LOWER( CONCAT('%', :searchTerm, '%') )
    """
    )
    fun searchContent(
        @Param("clsId") clsId: String,
        @Param("searchTerm") searchTerm: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>

    // 3) 작성자 검색
    @Query(
        value = """
            SELECT new org.eduai.educhat.dto.discussion.response.TopicHistoryResponseDto(
                t.sessionId,
                u.userNm,
                t.clsId,
                t.topicContent,
                t.insDt
            )
            FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId 
            WHERE t.clsId = :clsId
              AND t.delYn = 'N'
              AND LOWER(u.userNm) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
            ORDER BY t.insDt DESC
        """,
        countQuery = """
            SELECT count(t)
            FROM DiscTopic t LEFT JOIN UserMst u ON t.userId = u.userId
            WHERE t.clsId = :clsId
              AND t.delYn = 'N'
              AND LOWER(u.userNm) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        """
    )
    fun searchUser(
        @Param("clsId") clsId: String,
        @Param("searchTerm") searchTerm: String,
        pageable: Pageable
    ): Page<TopicHistoryResponseDto>


}