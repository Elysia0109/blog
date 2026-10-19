package com.niit.springboot.service.impl;

import com.niit.springboot.dto.request.ArticleRequest;
import com.niit.springboot.dto.response.ArticleDetailResponse;
import com.niit.springboot.dto.response.ArticleListResponse;
import com.niit.springboot.entity.Article;
import com.niit.springboot.entity.BrowseRecord;
import com.niit.springboot.entity.User;
import com.niit.springboot.mapper.*;
import com.niit.springboot.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final BrowseRecordMapper browseRecordMapper;
    private final LikeMapper likeMapper;

    @Override
    @Transactional
    public Integer createArticle(ArticleRequest request, Integer authorId) {
        log.info("创建文章，作者ID: {}, 标题: {}", authorId, request.getTitle());

        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new RuntimeException("用户不存在");
        }

        Article article = new Article();
        BeanUtils.copyProperties(request, article);
        article.setAuthorId(authorId);
        article.setAuthorName(author.getUsername());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setCommentCount(0);
        article.setStatus(1);
        article.setPublishTime(LocalDateTime.now());
        article.setIsDeleted(0);
        article.setIsTop(0);

        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String content = article.getContent();
            if (content != null && !content.isEmpty()) {
                String summary = content.replaceAll("<[^>]+>", "");
                article.setSummary(summary.length() > 100 ? summary.substring(0, 100) + "..." : summary);
            } else {
                article.setSummary("暂无摘要");
            }
        }

        int result = articleMapper.insert(article);
        if (result <= 0) {
            throw new RuntimeException("文章创建失败");
        }

        log.info("文章创建成功，ID: {}", article.getId());
        return article.getId();
    }

    @Override
    @Transactional
    public void updateArticle(Integer id, ArticleRequest request, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限修改此文章");
        }

        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setIsTop(request.getIsTop() != null ? request.getIsTop() : 0);

        articleMapper.update(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Integer id, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限删除此文章");
        }

        articleMapper.deleteById(id);
    }

    @Override
    public ArticleDetailResponse getArticleDetail(Integer id) {
        log.info("获取文章详情，ID: {}", id);

        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在或已被删除");
        }

        if (article.getStatus() != 1) {
            throw new RuntimeException("文章未发布");
        }

        ArticleDetailResponse response = new ArticleDetailResponse();
        BeanUtils.copyProperties(article, response);

        // 获取作者信息，设置头像
        User author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            response.setAuthorAvatar(author.getAvatar());
        }

        // 返回前增加阅读量
        articleMapper.incrementViewCount(id);

        log.info("文章详情获取成功: {}", response.getTitle());
        return response;
    }

    @Override
    public List<ArticleListResponse> getArticleList(Integer page, Integer size) {
        int offset = (page - 1) * size;
        return articleMapper.selectArticleList(offset, size);
    }

    @Override
    public List<ArticleListResponse> getArticlesByCategory(String category, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return articleMapper.selectArticlesByCategory(category, offset, size);
    }

    @Override
    public List<ArticleListResponse> getUserArticles(Integer authorId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return articleMapper.selectArticlesByAuthor(authorId, offset, size);
    }

    @Override
    public List<ArticleListResponse> getFavoriteArticles(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Integer> articleIds = favoriteMapper.selectArticleIdsByUser(userId);
        if (articleIds.isEmpty()) {
            return List.of();
        }
        return articleMapper.selectArticlesByIds(articleIds, offset, size);
    }

    @Override
    public List<ArticleListResponse> getBrowseRecords(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return browseRecordMapper.selectRecentBrowseArticles(userId, offset, size);
    }

    @Override
    public List<ArticleListResponse> searchArticles(String keyword, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return articleMapper.searchArticles(keyword, offset, size);
    }

    @Override
    @Transactional
    public void toggleTop(Integer id, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限操作此文章");
        }

        int newIsTop = article.getIsTop() == 1 ? 0 : 1;
        articleMapper.toggleTop(id, newIsTop);
    }

    @Override
    @Transactional
    public void addBrowseRecord(Integer userId, Integer articleId) {
        try {
            BrowseRecord record = new BrowseRecord();
            record.setUserId(userId);
            record.setArticleId(articleId);
            record.setBrowseTime(LocalDateTime.now());
            record.setDuration(30); // 默认30秒

            browseRecordMapper.insert(record);
            log.info("添加浏览记录成功，用户ID: {}, 文章ID: {}", userId, articleId);
        } catch (Exception e) {
            log.error("添加浏览记录失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void likeArticle(Integer articleId, Integer userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在或已被删除");
        }

        // 检查是否已点赞
        boolean alreadyLiked = likeMapper.existsByUserAndArticle(userId, articleId);
        if (!alreadyLiked) {
            likeMapper.insert(userId, articleId);
            articleMapper.updateLikeCount(articleId, 1);
            log.info("用户 {} 点赞文章 {}", userId, articleId);
        }
    }

    @Override
    @Transactional
    public void cancelLike(Integer articleId, Integer userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在或已被删除");
        }

        // 检查是否已点赞
        boolean alreadyLiked = likeMapper.existsByUserAndArticle(userId, articleId);
        if (alreadyLiked) {
            likeMapper.delete(userId, articleId);
            articleMapper.updateLikeCount(articleId, -1);
            log.info("用户 {} 取消点赞文章 {}", userId, articleId);
        }
    }

    @Override
    @Transactional
    public void incrementViewCount(Integer articleId) {
        articleMapper.incrementViewCount(articleId);
    }

    @Override
    public Integer countArticles() {
        return articleMapper.countArticles();
    }

    @Override
    public Integer countArticlesByCategory(String category) {
        return articleMapper.countArticlesByCategory(category);
    }

    @Override
    public Integer countArticlesByAuthor(Integer authorId) {
        return articleMapper.countArticlesByAuthor(authorId);
    }

    @Override
    public Integer countBrowseRecords(Integer userId) {
        return browseRecordMapper.countByUser(userId);
    }

    @Override
    public Integer countSearchArticles(String keyword) {
        return articleMapper.countSearchArticles(keyword);
    }

    // 新增方法：检查用户是否点赞文章
    public boolean isLiked(Integer articleId, Integer userId) {
        return likeMapper.existsByUserAndArticle(userId, articleId);
    }
}