package org.eduai.educhat.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:3000",
            "http://58.29.36.4:3500",
            "https://tutor.k-university.ai",
            "http://27.96.151.215:3500")
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
