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

    fun generatePendingMessagesKey(clsId: String, grpId: String): String {
        return "pending_messages:$clsId:$grpId"
    }

    fun generateChatLogsKey(clsId: String, grpId: String): String {
        return "chat_logs:$clsId:$grpId"
    }

    fun decodeRedisSessionKey(sessionKey: String): String {
        return sessionKey.split(":")[1]
    }

    fun decodeRedisSessionHashKey(sessionHashKey: String): String {
        return sessionHashKey.split(":")[1]
    }
}
