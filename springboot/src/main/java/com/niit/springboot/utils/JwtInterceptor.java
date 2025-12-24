package com.niit.springboot.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        // 从Authorization头获取token并解析用户ID
        String token = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", token);

        if (token != null) {
            try {
                if (token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                    logger.debug("Extracted token: {}", token);
                }
                // 验证token并获取userId
                logger.debug("Validating token...");
                if (jwtUtil.validateToken(token)) {
                    Integer userId = jwtUtil.getUserIdFromToken(token);
                    logger.debug("Token valid, setting userId: {}", userId);
                    // 设置用户ID到请求属性中
                    request.setAttribute("userId", userId);
                } else {
                    logger.debug("Token validation failed");
                }
            } catch (Exception e) {
                logger.error("Token parsing error: {}", e.getMessage());
            }
        } else {
            logger.debug("No Authorization header found");
        }
        return true;
    }
}