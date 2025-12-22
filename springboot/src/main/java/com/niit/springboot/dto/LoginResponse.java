package com.niit.springboot.dto;

import java.util.Map;

public class LoginResponse {
    private int code;
    private String message;
    private String token;
    private Map<String, Object> userInfo; // 修改字段名为 userInfo

    public LoginResponse() {}

    public LoginResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public LoginResponse(int code, String message, String token, Map<String, Object> userInfo) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.userInfo = userInfo; // 修改构造参数
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getUserInfo() { // 修改 getter 方法名
        return userInfo;
    }

    public void setUserInfo(Map<String, Object> userInfo) { // 修改 setter 方法名
        this.userInfo = userInfo;
    }
}