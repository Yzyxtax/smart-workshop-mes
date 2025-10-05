package com.xtax.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有路由
                .allowedOriginPatterns("*") // 允许的前端地址
                .allowedMethods("*") // 允许的方法
                .allowedHeaders("*") // 允许所有请求头（或指定具体头，如 "Content-Type", "Authorization"）
                .allowCredentials(true) // 允许携带凭证（如 Cookie）
                .maxAge(3600); // 预检请求缓存时间（单位：秒）
    }
}

