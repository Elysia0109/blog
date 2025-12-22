package com.niit.springboot.service.impl;

import com.niit.springboot.dto.request.ArticleRequest;
import com.niit.springboot.dto.response.ArticleDetailResponse;
import com.niit.springboot.dto.response.ArticleListResponse;
import com.niit.springboot.entity.Article;
import com.niit.springboot.entity.User;
import com.niit.springboot.mapper.ArticleMapper;
import com.niit.springboot.mapper.FavoriteMapper;
import com.niit.springboot.mapper.UserMapper;
import com.niit.springboot.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public Integer createArticle(ArticleRequest request, Integer authorId) {
        // 获取作者信息
        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建文章
        Article article = new Article();
        BeanUtils.copyProperties(request, article);
        article.setAuthorId(authorId);
        article.setAuthorName(author.getUsername());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setCommentCount(0);
        article.setStatus(1); // 已发布
        article.setPublishTime(LocalDateTime.now());
        article.setIsDeleted(0); // 未删除
        article.setIsTop(0); // 默认不置顶
        
        // 如果没有提供摘要，从内容中生成
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            // 从内容中截取前100个字符作为摘要
            String content = article.getContent();
            if (content != null) {
                article.setSummary(content.length() > 100 ? content.substring(0, 100) + "..." : content);
            } else {
                article.setSummary("");
            }
        }

        // 插入文章，MyBatis会将生成的ID设置到article对象的id属性中
        int result = articleMapper.insert(article);
        if (result <= 0) {
            throw new RuntimeException("文章创建失败");
        }
        return article.getId();
    }

    @Override
    @Transactional
    public void updateArticle(Integer id, ArticleRequest request, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 检查权限：只能修改自己的文章
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限修改此文章");
        }

        // 更新文章信息
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setIsTop(request.getIsTop());

        articleMapper.update(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Integer id, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 检查权限
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限删除此文章");
        }

        // 逻辑删除
        articleMapper.deleteById(id);
    }

    @Override
    public ArticleDetailResponse getArticleDetail(Integer id) {
        // 增加阅读量
        articleMapper.incrementViewCount(id);

        // 获取文章详情
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在或已被删除");
        }

        if (article.getStatus() != 1) {
            throw new RuntimeException("文章未发布");
        }

        // 转换为响应对象
        ArticleDetailResponse response = new ArticleDetailResponse();
        BeanUtils.copyProperties(article, response);

        // 设置是否收藏（这里简化处理，实际需要当前用户ID）
        response.setIsFavorite(false);
        response.setIsLiked(false);

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
    @Transactional
    public void toggleTop(Integer id, Integer authorId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 检查权限
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权限操作此文章");
        }

        // 切换置顶状态
        int newIsTop = article.getIsTop() == 1 ? 0 : 1;
        articleMapper.toggleTop(id, newIsTop);
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
}