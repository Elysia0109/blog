package com.niit.springboot.service;

import com.niit.springboot.dto.request.LoginRequest;
import com.niit.springboot.dto.request.RegisterRequest;
import com.niit.springboot.dto.response.UserInfoResponse;

public interface UserService {

    /**
     * 用户注册
     */
    UserInfoResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    String login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUserInfo(Integer userId);

    /**
     * 更新用户信息
     */
    UserInfoResponse updateUserInfo(Integer userId, UserInfoResponse userInfo);

    /**
     * 修改密码
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);
}