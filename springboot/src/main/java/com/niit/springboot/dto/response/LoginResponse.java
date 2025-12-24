package com.niit.springboot.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class LoginResponse {
    private int code;
    private String message;
    private String token;
    private Map<String, Object> userInfo;

    public LoginResponse() {}

    public LoginResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public LoginResponse(int code, String message, String token, Map<String, Object> userInfo) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.userInfo = userInfo;
    }
}