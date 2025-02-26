//package org.eduai.educhat.batch
//
//import org.eduai.educhat.dto.RedisMessageDto
//import org.eduai.educhat.entity.DiscThreadHist
//import org.eduai.educhat.repository.DiscThreadHistRepository
//import org.eduai.educhat.service.KeyGeneratorService
//import org.eduai.educhat.util.batch.PostgresItemWriter
//import org.eduai.educhat.util.batch.RedisItemReader
//import org.eduai.educhat.util.batch.RedisToPostgresProcessor
//import org.springframework.batch.core.*
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
//import org.springframework.batch.core.job.builder.JobBuilder
//import org.springframework.batch.core.repository.JobRepository
//import org.springframework.batch.core.step.builder.StepBuilder
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.redis.core.StringRedisTemplate
//import org.springframework.transaction.PlatformTransactionManager
//
//@Configuration
//@EnableBatchProcessing
//class BatchConfig(
//    private val jobRepository: JobRepository,
//    private val transactionManager: PlatformTransactionManager,
//    private val redisTemplate: StringRedisTemplate,
//    private val discThreadHistRepository: DiscThreadHistRepository,
//    private val keyGeneratorService: KeyGeneratorService
//) {
//
//    @Bean
//    fun redisToPostgresStep(): Step {
//        return StepBuilder("redisToPostgresStep", jobRepository)
//            .chunk<List<RedisMessageDto>, DiscThreadHist>(1, transactionManager)
//            .reader(RedisItemReader(redisTemplate, keyGeneratorService))
//            .processor(RedisToPostgresProcessor())
//            .writer(PostgresItemWriter(discThreadHistRepository))
//            .build()
//    }
//
//    @Bean
//    fun redisToPostgresJob(): Job {
//        return JobBuilder("redisToPostgresJob", jobRepository)
//            .start(redisToPostgresStep())
//            .build()
//    }
//}