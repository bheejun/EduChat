package org.eduai.educhat.service

import org.springframework.stereotype.Service

@Service
class KeyGeneratorService {

    fun generateRedisSessionKey(clsId: String): String {
        return "cls_sessions_prefix:$clsId"
    }

    fun generateRedisSessionHashKey(groupId: String) : String{
        return "grp_prefix:$groupId"
    }

    fun generateRedisLogKey(clsId: String, groupId: String): String {
        return "chat_logs_prefix:$clsId:$groupId"
    }

    fun generatePostgresLogKey(clsId: String, groupId: String): String {
        return "chat_logs_prefix:$clsId:$groupId"
    }

    fun generateChunkKey(clsId: String, groupId: String, chunkId: String): String {
        return "chat_logs_prefix:$clsId:$groupId:$chunkId"
    }

    fun generateRestoreKey(): String {
        return "cls_sessions_prefix:*"
    }
}