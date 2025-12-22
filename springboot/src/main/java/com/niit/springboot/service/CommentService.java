package com.niit.springboot.service;

import com.niit.springboot.dto.request.CommentRequest;
import com.niit.springboot.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {

    // 添加评论
    Integer addComment(CommentRequest request, Integer userId);

    // 删除评论
    void deleteComment(Integer id, Integer userId);

    // 获取文章评论
    List<CommentResponse> getCommentsByArticleId(Integer articleId);

    // 获取评论数量
    Integer countCommentsByArticleId(Integer articleId);
}