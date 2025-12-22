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

        // 2. 允许的源（这里设置为允许所有，实际生产环境应该配置具体域名）
        config.addAllowedOriginPattern("*");  // 使用addAllowedOriginPattern替代addAllowedOrigin

        // 3. 允许携带凭证（如cookies）
        config.setAllowCredentials(true);

        // 4. 允许的请求方法
        config.addAllowedMethod("*");  // 允许所有方法：GET, POST, PUT, DELETE等

        // 5. 允许的请求头
        config.addAllowedHeader("*");  // 允许所有请求头

        // 6. 暴露的响应头
        config.addExposedHeader("Authorization");  // 允许前端获取Authorization头
        config.addExposedHeader("Content-Disposition");

        // 7. 预检请求的有效期（单位：秒）
        config.setMaxAge(3600L);

        // 8. 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 9. 对所有接口路径应用CORS配置
        source.registerCorsConfiguration("/**", config);

        // 10. 返回CORS过滤器
        return new CorsFilter(source);
    }
}