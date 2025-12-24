package com.niit.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 允许的源
        config.addAllowedOriginPattern("*");

        config.setAllowCredentials(true);

        config.addAllowedMethod("*");  // 允许所有方法：GET, POST, PUT, DELETE等


        config.addAllowedHeader("*");  // 允许所有请求头


        config.addExposedHeader("Authorization");  // 允许前端获取Authorization头
        config.addExposedHeader("Content-Disposition");

        config.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration("/**", config);


        return new CorsFilter(source);
    }
}