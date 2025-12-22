package com.niit.springboot.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String account;  // 去掉 @NotBlank 注解
    private String password; // 去掉 @NotBlank 注解
}