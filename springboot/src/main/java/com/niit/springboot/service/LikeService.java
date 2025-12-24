package com.niit.springboot.service;

public interface LikeService {
    // 点赞
    void like(Integer articleId, Integer userId);

    // 取消点赞
    void cancelLike(Integer articleId, Integer userId);

    // 检查是否点赞
    boolean isLiked(Integer articleId, Integer userId);

    // 获取文章点赞数量
    int getArticleLikeCount(Integer articleId);
}