//package org.eduai.educhat.config
//
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.servlet.config.annotation.CorsRegistry
//import org.springframework.web.servlet.config.annotation.EnableWebMvc
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
//
//
//@Configuration
//@EnableWebMvc
//class WebConfig : WebMvcConfigurer  {
//
//    //나중에 security 설정할 때 cors 설정을 해주어야 한다.
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//            .allowedOrigins("*")
//            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//            .allowedHeaders("*")
//            .allowCredentials(false)
//    }
//
//}