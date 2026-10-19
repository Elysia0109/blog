// SettingController.java
package com.niit.springboot.controller;

import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.UserInfoResponse;
import com.niit.springboot.service.UserService;
import com.niit.springboot.utils.OSSUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final UserService userService;
    private final OSSUtil ossUtil;

    // 获取用户设置信息
    @GetMapping("/profile")
    public ApiResponse<UserInfoResponse> getProfile(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        UserInfoResponse userInfo = userService.getCurrentUserInfo(userId);
        return ApiResponse.success(userInfo);
    }

    // 更新用户基本信息
    @PutMapping("/profile")
    public ApiResponse<UserInfoResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody UserInfoResponse userInfo) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        // 验证必要字段
        if (!StringUtils.hasText(userInfo.getUsername())) {
            return ApiResponse.error(400, "用户名不能为空");
        }

        UserInfoResponse updatedUser = userService.updateUserInfo(userId, userInfo);
        return ApiResponse.success(updatedUser);
    }

    // 上传头像
    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error(400, "只能上传图片文件");
            }

            // 验证文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ApiResponse.error(400, "图片大小不能超过5MB");
            }

            // 上传到阿里云OSS
            String avatarUrl = ossUtil.uploadFile(file, "avatars/");

            // 更新用户头像信息
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setAvatar(avatarUrl);
            userService.updateUserInfo(userId, userInfo);

            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("avatar", avatarUrl);
            result.put("message", "头像上传成功");

            log.info("用户 {} 头像上传成功: {}", userId, avatarUrl);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, "头像上传失败: " + e.getMessage());
        }
    }

    // 修改密码
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        try {
            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                return ApiResponse.error(400, "新密码至少6位");
            }

            userService.changePassword(userId, oldPassword, newPassword);

            log.info("用户 {} 修改密码成功", userId);
            return ApiResponse.success();

        } catch (RuntimeException e) {
            log.error("修改密码失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("修改密码异常: {}", e.getMessage(), e);
            return ApiResponse.error(500, "修改密码失败");
        }
    }

    // 更新邮箱
    @PutMapping("/email")
    public ApiResponse<UserInfoResponse> updateEmail(
            HttpServletRequest request,
            @RequestParam String email) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        try {
            // 简单的邮箱格式验证
            if (!StringUtils.hasText(email) || !email.contains("@")) {
                return ApiResponse.error(400, "邮箱格式不正确");
            }

            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setEmail(email);
            UserInfoResponse updatedUser = userService.updateUserInfo(userId, userInfo);

            log.info("用户 {} 更新邮箱成功: {}", userId, email);
            return ApiResponse.success(updatedUser);

        } catch (Exception e) {
            log.error("更新邮箱失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, "更新邮箱失败");
        }
    }

    // 从请求属性中获取用户ID
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }
}