package com.niit.springboot.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String account;   // 去掉注解
    private String username;  // 去掉注解
    private String password;  // 去掉注解
    private String nickname;
    private String email;
}