package com.niit.springboot.controller;

import com.niit.springboot.dto.request.CommentRequest;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.CommentResponse;
import com.niit.springboot.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.CommentResponse;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 添加评论 - 修改为从请求属性获取用户ID
    @PostMapping
    public ApiResponse<Integer> addComment(@RequestBody CommentRequest request,
                                           HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }

        Integer commentId = commentService.addComment(request, userId);
        return ApiResponse.success(commentId);
    }

    // 删除评论 - 修改为从请求属性获取用户ID
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Integer id,
                                           HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }

        commentService.deleteComment(id, userId);
        return ApiResponse.success();
    }

    // 获取文章评论
    @GetMapping("/article/{articleId}")
    public ApiResponse<List<CommentResponse>> getCommentsByArticleId(@PathVariable Integer articleId) {
        List<CommentResponse> comments = commentService.getCommentsByArticleId(articleId);
        return ApiResponse.success(comments);
    }

    // 获取评论数量
    @GetMapping("/article/{articleId}/count")
    public ApiResponse<Integer> countCommentsByArticleId(@PathVariable Integer articleId) {
        Integer count = commentService.countCommentsByArticleId(articleId);
        return ApiResponse.success(count);
    }

    // 从请求属性中获取用户ID（由JwtInterceptor设置）
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }
}