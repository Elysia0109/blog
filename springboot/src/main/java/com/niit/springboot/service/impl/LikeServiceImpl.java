package com.niit.springboot.service.impl;

import com.niit.springboot.mapper.LikeMapper;
import com.niit.springboot.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;

    @Override
    @Transactional
    public void like(Integer articleId, Integer userId) {
        if (!likeMapper.existsByUserAndArticle(userId, articleId)) {
            likeMapper.insert(userId, articleId);
        }
    }

    @Override
    @Transactional
    public void cancelLike(Integer articleId, Integer userId) {
        likeMapper.delete(userId, articleId);
    }

    @Override
    public boolean isLiked(Integer articleId, Integer userId) {
        return likeMapper.existsByUserAndArticle(userId, articleId);
    }

    @Override
    public int getArticleLikeCount(Integer articleId) {
        return likeMapper.countByArticle(articleId);
    }
}