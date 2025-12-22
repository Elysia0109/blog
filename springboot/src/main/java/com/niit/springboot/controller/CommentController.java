package com.niit.springboot.controller;

import com.niit.springboot.dto.request.CommentRequest;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.CommentResponse;
import com.niit.springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ApiResponse<Integer> addComment(@RequestBody CommentRequest request,
                                           @RequestHeader("X-User-ID") Integer userId) {
        Integer commentId = commentService.addComment(request, userId);
        return ApiResponse.success(commentId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Integer id,
                                           @RequestHeader("X-User-ID") Integer userId) {
        commentService.deleteComment(id, userId);
        return ApiResponse.success();
    }

    @GetMapping("/article/{articleId}")
    public ApiResponse<List<CommentResponse>> getCommentsByArticleId(@PathVariable Integer articleId) {
        List<CommentResponse> comments = commentService.getCommentsByArticleId(articleId);
        return ApiResponse.success(comments);
    }

    @GetMapping("/article/{articleId}/count")
    public ApiResponse<Integer> countCommentsByArticleId(@PathVariable Integer articleId) {
        Integer count = commentService.countCommentsByArticleId(articleId);
        return ApiResponse.success(count);
    }
}