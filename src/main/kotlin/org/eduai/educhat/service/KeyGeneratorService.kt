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

    fun generateCurrentChunkKey(clsId: String, grpId: String): String {
        return "chat_logs:$clsId:$grpId:current_chunk"
    }

    // 특정 청크 번호에 해당하는 메시지 로그 키
    fun generateLogKey(clsId: String, grpId: String, chunkIndex: Int): String {
        return "chat_logs:$clsId:$grpId:$chunkIndex"
    }

    fun generateRestoreKey(): String {
        return "cls_sessions_prefix:*"
    }
}