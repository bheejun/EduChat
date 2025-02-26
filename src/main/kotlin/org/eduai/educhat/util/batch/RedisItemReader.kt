//package org.eduai.educhat.util.batch
//
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import org.eduai.educhat.dto.RedisMessageDto
//import org.eduai.educhat.service.KeyGeneratorService
//import org.slf4j.LoggerFactory
//import org.springframework.batch.item.ItemReader
//import org.springframework.data.redis.core.StringRedisTemplate
//
//class RedisItemReader(
//    private val redisTemplate: StringRedisTemplate,
//    private val keyGeneratorService: KeyGeneratorService
//) : ItemReader<List<RedisMessageDto>> {
//
//    private val logger = LoggerFactory.getLogger(RedisItemReader::class.java)
//    private val objectMapper = jacksonObjectMapper()
//    private val messagesQueue = mutableListOf<RedisMessageDto>()
//
//    override fun read(): List<RedisMessageDto>? {
//        if (messagesQueue.isEmpty()) {
//            loadMessagesFromRedis()
//        }
//        return if (messagesQueue.isNotEmpty()) {
//            val chunk = messagesQueue.take(100)  // ✅ 100개씩 묶어서 반환
//            messagesQueue.removeAll(chunk)
//            chunk
//        } else {
//            null
//        }
//    }
//
//    private fun loadMessagesFromRedis() {
//        val sessionKeys = redisTemplate.keys("chat_logs:*:*:current_chunk")
//
//        sessionKeys.forEach { currentChunkKey ->
//            val keys = currentChunkKey.split(":")
//            val clsId = keys[1]
//            val grpId = keys[2]
//            val chunkCount = redisTemplate.opsForValue().get(currentChunkKey)?.toIntOrNull() ?: 0
//
//            for (chunkIndex in 0..chunkCount) {
//                val logKey = keyGeneratorService.generateLogKey(clsId, grpId, chunkIndex)
//                val messageList = redisTemplate.opsForList().range(logKey, 0, -1) ?: emptyList()
//
//                messageList.forEach { json ->
//                    try {
//                        val dto: RedisMessageDto = objectMapper.readValue(json)
//                        messagesQueue.add(dto)
//                    } catch (e: Exception) {
//                        logger.error("❌ JSON 파싱 오류: $json", e)
//                    }
//                }
//
//                redisTemplate.delete(logKey)
//            }
//
//            redisTemplate.delete(currentChunkKey)
//        }
//    }
//}