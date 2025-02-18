package org.eduai.educhat.service

import org.springframework.stereotype.Service

@Service
class KeyGeneratorService {

    fun generateRedisSessionKey(clsId: String): String {
        return "chat_sessions:$clsId"
    }

    fun generateRedisSessionHashKey(groupId: String) : String{
        return "grp_prefix:$groupId"
    }

    fun generateRedisLogKey(clsId: String, groupId: String): String {
        return "chat_logs:$clsId:$groupId"
    }

    fun generatePostgresLogKey(clsId: String, groupId: String): String {
        return "chat_logs:$clsId:$groupId"
    }

    fun generateRedisLogChunckKey(clsId: String, groupId: String, chunkId: String): String {
        return "chat_logs:$clsId:$groupId:$chunkId"
    }
}