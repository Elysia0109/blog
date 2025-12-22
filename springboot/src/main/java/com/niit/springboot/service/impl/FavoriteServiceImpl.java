package com.niit.springboot.service.impl;

import com.niit.springboot.entity.Favorite;
import com.niit.springboot.mapper.FavoriteMapper;
import com.niit.springboot.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public void addFavorite(Integer userId, Integer articleId) {
        // 检查是否已收藏
        if (!favoriteMapper.existsByUserAndArticle(userId, articleId)) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favoriteMapper.insert(favorite);
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Integer userId, Integer articleId) {
        favoriteMapper.delete(userId, articleId);
    }

    @Override
    public boolean isFavorite(Integer userId, Integer articleId) {
        return favoriteMapper.existsByUserAndArticle(userId, articleId);
    }

    @Override
    public List<Integer> getUserFavorites(Integer userId) {
        return favoriteMapper.selectArticleIdsByUser(userId);
    }

    @Override
    public int getArticleFavoriteCount(Integer articleId) {
        return favoriteMapper.countByArticle(articleId);
    }
}