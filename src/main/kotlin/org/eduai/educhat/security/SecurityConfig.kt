package org.eduai.educhat.security

import jakarta.servlet.Filter
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.ObjectPostProcessor
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

//    @Bean
//    fun corsConfig(): CorsConfigurationSource {
//        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = listOf("http://localhost:3000") // ✅ React의 주소 허용
//        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
//        configuration.allowedHeaders = listOf("*")
//        configuration.allowCredentials = true
//        configuration.allowedOriginPatterns = listOf("http://localhost:3000", "http://172.17.0.2:6379")
//
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", configuration)
//        return source
//    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("http://localhost:3000", "http://58.29.36.4:3500")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true // 인증 정보 포함 허용

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        http
            .cors { it.configurationSource(source) }
            .csrf { it.disable() } // CSRF 비활성화
            .authorizeHttpRequests { it.anyRequest().permitAll() } // 모든 요청 허용
            .formLogin { it.disable() } // 로그인 비활성화
            .httpBasic { it.disable() } // Basic Auth 제거

        return http.build()
    }

}
