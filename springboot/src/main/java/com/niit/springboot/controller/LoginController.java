package com.niit.springboot.controller;

import com.niit.springboot.dto.request.LoginRequest;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.LoginResponse;
import com.niit.springboot.service.UserService;
import com.niit.springboot.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 调用UserService的login方法获取token
            String token = userService.login(request);

            // 获取用户信息
            var user = userMapper.selectByAccount(request.getAccount());
            var userInfo = userService.getCurrentUserInfo(user.getId());

            // 创建用户信息Map
            Map<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("id", userInfo.getId());
            userInfoMap.put("username", userInfo.getUsername());
            userInfoMap.put("nickname", userInfo.getNickname());
            userInfoMap.put("avatar", userInfo.getAvatar());

            // 创建LoginResponse
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setCode(200);
            loginResponse.setMessage("登录成功");
            loginResponse.setToken(token);
            loginResponse.setUserInfo(userInfoMap);

            return ApiResponse.success(loginResponse);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }
}