package com.niit.springboot.service;

import java.util.List;

public interface FavoriteService {
    // 添加收藏
    void addFavorite(Integer userId, Integer articleId);

    // 取消收藏
    void removeFavorite(Integer userId, Integer articleId);

    // 检查是否收藏
    boolean isFavorite(Integer userId, Integer articleId);

    // 获取用户收藏列表
    List<Integer> getUserFavorites(Integer userId);

    // 获取用户收藏数量
    int getUserFavoriteCount(Integer userId);
}