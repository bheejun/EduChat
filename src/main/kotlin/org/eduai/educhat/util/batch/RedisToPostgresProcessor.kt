//package org.eduai.educhat.util.batch
//
//import org.eduai.educhat.dto.RedisMessageDto
//import org.eduai.educhat.entity.DiscThreadHist
//import org.springframework.batch.item.ItemProcessor
//import java.util.*
//
//class RedisToPostgresProcessor : ItemProcessor<List<RedisMessageDto>, DiscThreadHist> {
//    override fun process(items: List<RedisMessageDto>): DiscThreadHist? {
//        if (items.isEmpty()) return null
//        val sessionId = UUID.randomUUID()  // Session ID 생성
//        val grpId = UUID.randomUUID()  // Group ID 생성
//
//        return DiscThreadHist.fromMessages(sessionId, grpId, items)
//    }
//}
