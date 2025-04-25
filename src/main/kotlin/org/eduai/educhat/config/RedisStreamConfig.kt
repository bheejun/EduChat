package org.eduai.educhat.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import java.time.Duration

@Configuration
class RedisStreamConfig(
    private val factory: RedisConnectionFactory
) {
    @Bean
    fun streamListenerContainer(): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options: StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(20)
                .pollTimeout(Duration.ofSeconds(1))
                .build()
        val container = StreamMessageListenerContainer.create(factory, options)
        container.start()
        return container
    }
}