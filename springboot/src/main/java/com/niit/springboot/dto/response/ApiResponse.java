package com.niit.springboot.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // 分页响应
    public static <T> ApiResponse<PageResponse<T>> pageSuccess(Integer page, Integer size, Integer total, List<T> items) {
        PageResponse<T> pageResponse = new PageResponse<>(page, size, total, items);
        return success(pageResponse);
    }
}