package com.niit.springboot.service.impl;

import com.niit.springboot.dto.request.LoginRequest;
import com.niit.springboot.dto.request.RegisterRequest;
import com.niit.springboot.dto.response.UserInfoResponse;
import com.niit.springboot.entity.User;
import com.niit.springboot.mapper.UserMapper;
import com.niit.springboot.service.UserService;
import com.niit.springboot.utils.JwtUtil;
import com.niit.springboot.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserInfoResponse register(RegisterRequest request) {
        // 检查账号是否已存在
        int count = userMapper.countByAccount(request.getAccount());
        if (count > 0) {
            throw new RuntimeException("账号已存在");
        }

        // 创建用户
        User user = new User();
        user.setAccount(request.getAccount());
        user.setUsername(request.getUsername());
        user.setNickname(StringUtils.hasText(request.getNickname()) ?
                request.getNickname() : request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setAvatar("https://niit-pro.oss-cn-guangzhou.aliyuncs.com/avatars/88ac8441e22344a3a67ea0c262966191.ico?Expires=1766627914&OSSAccessKeyId=TMP.3Ko88rXTiUBxPFcmo7FPcbNPQSaLszYKvyyRLt1hNKHCqcpomC5W41bW8wmYAW4Cc2TcHvKZh1gtK1bqiaM8HfuJK1xNSK&Signature=N3fyiluGzaH%2FSI6kLVFWlaMvTTY%3D");
        user.setStatus(1); // 正常状态

        userMapper.insert(user);

        return convertToUserInfoResponse(user);
    }

    @Override
    public String login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByAccount(request.getAccount());
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }

        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 使用JwtUtil生成JWT token
        return jwtUtil.generateToken(user.getId(), user.getAccount());
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public UserInfoResponse updateUserInfo(Integer userId, UserInfoResponse userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        boolean updated = false;
        if (StringUtils.hasText(userInfo.getUsername())) {
            user.setUsername(userInfo.getUsername());
            updated = true;
        }
        if (StringUtils.hasText(userInfo.getNickname())) {
            user.setNickname(userInfo.getNickname());
            updated = true;
        }
        if (StringUtils.hasText(userInfo.getAvatar())) {
            user.setAvatar(userInfo.getAvatar());
            updated = true;
        }
        if (StringUtils.hasText(userInfo.getEmail())) {
            user.setEmail(userInfo.getEmail());
            updated = true;
        }

        if (updated) {
            userMapper.update(user);
        }

        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        userMapper.updatePassword(userId, PasswordUtil.encode(newPassword));
    }

    private UserInfoResponse convertToUserInfoResponse(User user) {
        if (user == null) return null;

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setAccount(user.getAccount());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        return response;
    }
}