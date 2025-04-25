package org.eduai.educhat.service

import org.springframework.stereotype.Service

@Service
class KeyGeneratorService {

    fun generateRedisSessionKey(clsId: String): String {
        return "cls_sessions:$clsId"
    }

    fun generateRedisSessionHashKey(groupId: String): String {
        return "grp:$groupId"
    }

    fun generateChatLogsKey(clsId: String, grpId: String): String {
        return "chat_logs:$clsId:$grpId"
    }

    fun generateStreamKey(clsId: String, grpId: String): String = "log_stream:$clsId:$grpId"

    fun getStreamKeyPattern(): String = "log_stream:*:*"

    fun getSessionKeyPattern(): String = "cls_sessions:*"


    fun decodeRedisSessionKey(sessionKey: String): String {
        return sessionKey.split(":")[1]
    }

    fun decodeRedisSessionHashKey(sessionHashKey: String): String {
        return sessionHashKey.split(":")[1]
    }
}
