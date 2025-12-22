package com.niit.springboot.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.expiration:86400000}") // 24小时
    private Long expiration;

    // 使用固定的密钥，确保每次应用重启后都能验证之前生成的token
    private final SecretKey signingKey;

    public JwtUtil() {
        // 使用固定的密钥字符串，转换为SecretKey
        String secretString = "your-secret-key-123456789012345678901234567890";
        this.signingKey = Keys.hmacShaKeyFor(secretString.getBytes());
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    // 修改方法签名：第一个参数改为Integer
    public String generateToken(Integer userId, String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(account)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析Token的方法保持不变
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Integer.class);
    }

    public String getAccountFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("account", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}