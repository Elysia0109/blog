package com.niit.springboot.controller;

import com.niit.springboot.dto.request.LoginRequest;
import com.niit.springboot.dto.request.RegisterRequest;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.UserInfoResponse;
import com.niit.springboot.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserInfoResponse> register(@RequestBody RegisterRequest request) {
        if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
            return ApiResponse.error(400, "账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUserInfo(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        return ApiResponse.success(userService.getCurrentUserInfo(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserInfoResponse> updateUserInfo(
            HttpServletRequest httpRequest,
            @RequestBody UserInfoResponse userInfo) {
        Integer userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        return ApiResponse.success(userService.updateUserInfo(userId, userInfo));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        userService.changePassword(userId, oldPassword, newPassword);
        return ApiResponse.success();
    }

    // 从请求属性中获取用户ID
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }
}