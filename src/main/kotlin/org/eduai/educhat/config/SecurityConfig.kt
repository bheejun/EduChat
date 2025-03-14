package org.eduai.educhat.config

import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
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
            "http://58.29.36.4",
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
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        return http.build()
    }


    @Bean
    fun cookieSerializer(): CookieSerializer {
        return DefaultCookieSerializer().apply {
            setCookieMaxAge(-1)
            setSameSite("Strict")          // cross-site 요청 허용
            setUseSecureCookie(false)     // 개발 환경에서는 false (HTTPS가 아닐 경우)
        }
    }

    @Bean
    fun httpSessionListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(se: HttpSessionEvent) {
                se.session.maxInactiveInterval = 1800
            }
        }
    }

}
