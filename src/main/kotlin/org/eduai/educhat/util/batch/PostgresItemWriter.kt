//package org.eduai.educhat.util.batch
//
//import org.eduai.educhat.entity.DiscThreadHist
//import org.eduai.educhat.repository.DiscThreadHistRepository
//import org.springframework.batch.item.Chunk
//import org.springframework.batch.item.ItemWriter
//
//class PostgresItemWriter(
//    private val discThreadHistRepository: DiscThreadHistRepository
//) : ItemWriter<DiscThreadHist> {
//
//    override fun write(chunk: Chunk<out DiscThreadHist>) {
//        discThreadHistRepository.saveAll(chunk.items)
//    }
//}