package org.eduai.educhat.service.discussion

// 필요한 import 문 추가
import org.eduai.educhat.common.handler.CustomWebSocketHandlerDecoratorFactory // TODO: 역할에 맞는 이름의 인터페이스/클래스 주입 고려 (예: WebSocketSessionRegistry)
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession // WebSocketSession import 추가
import java.io.IOException // IOException import 추가
import java.util.concurrent.ConcurrentHashMap

@Component
class ThreadSessionManager(
    private val sessionProvider: CustomWebSocketHandlerDecoratorFactory
) {

    private val threadSessions: MutableMap<String, MutableList<String>> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(ThreadSessionManager::class.java)

    fun addSessionToThread(threadId: String, sessionId: String) {
        threadSessions.computeIfAbsent(threadId) {
            logger.info("ThreadId '{}'에 대한 새 세션 목록 생성", threadId)
            mutableListOf() // 키가 없으면 새 리스트 생성
        }.add(sessionId) // 해당 리스트에 세션 ID 추가
        logger.info("세션 '{}'를 스레드 '{}'에 추가했습니다.", sessionId, threadId)
    }

    fun removeSessionFromThread(threadId: String, sessionId: String) {
        val removed = threadSessions[threadId]?.remove(sessionId) // 특정 스레드 리스트에서 세션 ID 제거
        if (removed == true) {
            logger.info("세션 '{}'를 스레드 '{}'에서 제거했습니다.", sessionId, threadId)
            if (threadSessions[threadId]?.isEmpty() == true) {
                threadSessions.remove(threadId)
                logger.info("스레드 '{}'의 빈 세션 목록을 제거했습니다.", threadId)
            }
        }
    }

    fun removeSession(sessionId: String) {
        var foundAndRemoved = false
        threadSessions.forEach { (threadId, sessionList) ->
            if (sessionList.remove(sessionId)) { // 리스트에서 세션 ID 제거 시도
                logger.info("세션 '{}'를 스레드 '{}'에서 (전체 제거 중) 제거했습니다.", sessionId, threadId)
                foundAndRemoved = true
                // 선택 사항: 리스트가 비면 해당 스레드 ID 항목 자체를 맵에서 제거
                if (sessionList.isEmpty()) {
                    threadSessions.remove(threadId)
                    logger.info("스레드 '{}'의 빈 세션 목록을 제거했습니다.", threadId)
                }
            }
        }
        if (!foundAndRemoved) {
            logger.warn("세션 '{}'를 전체 제거하려고 했으나, 어떤 스레드에서도 찾지 못했습니다.", sessionId)
        }
    }

    fun disconnectThreadSessions(threadId: String) {
        // 1. 해당 threadId에 속한 세션 ID 목록을 가져옵니다 (조회 시점의 스냅샷).
        val sessionIds = threadSessions[threadId]?.toList() // toList()로 불변 복사본 생성

        if (sessionIds == null || sessionIds.isEmpty()) {
            logger.warn("스레드 '{}'에서 연결 종료할 세션이 없습니다.", threadId)
            // 세션 ID 목록이 없더라도 해당 threadId 항목은 맵에서 제거하는 것이 깔끔할 수 있습니다.
            threadSessions.remove(threadId)
            return
        }

        logger.info("스레드 '{}'의 세션 {}개에 대한 연결 종료를 시도합니다.", threadId, sessionIds.size)
        var closedCount = 0

        // 2. 각 세션 ID에 대해 반복합니다.
        sessionIds.forEach { sessionId ->
            try {
                // 3. 세션 ID를 사용하여 실제 WebSocketSession 객체를 조회합니다.
                //    (주입된 sessionProvider 사용)
                val session: WebSocketSession? = sessionProvider.getSession(sessionId)

                if (session != null) {
                    // 4. 세션이 열려있는 경우, 명시적으로 close()를 호출합니다.
                    if (session.isOpen) {
                        logger.info("세션 '{}' (스레드 '{}') 연결을 종료합니다.", sessionId, threadId)
                        // 종료 상태와 이유를 명시적으로 전달할 수 있습니다.
                        session.close(CloseStatus.NORMAL.withReason("PAU"))
                        closedCount++
                    } else {
                        // 이미 닫혀있는 경우 로그만 남깁니다.
                        logger.warn("세션 '{}' (스레드 '{}')는 이미 닫혀있습니다.", sessionId, threadId)
                    }
                } else {
                    logger.warn("세션 '{}' (스레드 '{}')를 세션 제공자에서 찾을 수 없습니다.", sessionId, threadId)
                }
            } catch (e: IOException) {
                logger.error("세션 '{}' (스레드 '{}') 종료 중 IOException 발생: {}", sessionId, threadId, e.message)
            } catch (e: Exception) {
                logger.error("세션 '{}' (스레드 '{}') 종료 중 예상치 못한 오류 발생: {}", sessionId, threadId, e.message, e)
            }
        }

        val removedList = threadSessions.remove(threadId)
        logger.info("스레드 '{}'의 세션 연결 종료 완료. 시도한 세션 수: {}, 실제로 닫은 세션 수: {}, 맵 항목 제거 여부: {}",
            threadId, sessionIds.size, closedCount, removedList != null)
    }
}