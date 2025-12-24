package com.niit.springboot.service;

import com.niit.springboot.dto.request.ArticleRequest;
import com.niit.springboot.dto.response.ArticleDetailResponse;
import com.niit.springboot.dto.response.ArticleListResponse;

import java.util.List;

public interface ArticleService {

    // 创建文章
    Integer createArticle(ArticleRequest request, Integer authorId);

    // 更新文章
    void updateArticle(Integer id, ArticleRequest request, Integer authorId);

    // 删除文章（逻辑删除）
    void deleteArticle(Integer id, Integer authorId);

    // 获取文章详情
    ArticleDetailResponse getArticleDetail(Integer id);

    // 获取文章列表（分页）
    List<ArticleListResponse> getArticleList(Integer page, Integer size);

    // 根据分类获取文章
    List<ArticleListResponse> getArticlesByCategory(String category, Integer page, Integer size);

    // 获取用户文章
    List<ArticleListResponse> getUserArticles(Integer authorId, Integer page, Integer size);

    // 获取收藏文章
    List<ArticleListResponse> getFavoriteArticles(Integer userId, Integer page, Integer size);

    // 获取浏览记录
    List<ArticleListResponse> getBrowseRecords(Integer userId, Integer page, Integer size);

    // 搜索文章
    List<ArticleListResponse> searchArticles(String keyword, Integer page, Integer size);

    // 置顶/取消置顶
    void toggleTop(Integer id, Integer authorId);

    // 添加浏览记录
    void addBrowseRecord(Integer userId, Integer articleId);

    // 点赞文章
    void likeArticle(Integer articleId, Integer userId);

    // 取消点赞
    void cancelLike(Integer articleId, Integer userId);

    // 增加阅读量
    void incrementViewCount(Integer articleId);

    // 获取文章总数
    Integer countArticles();

    // 获取分类文章数
    Integer countArticlesByCategory(String category);

    // 获取用户文章数
    Integer countArticlesByAuthor(Integer authorId);

    // 获取浏览记录数量
    Integer countBrowseRecords(Integer userId);

    // 获取搜索结果数量
    Integer countSearchArticles(String keyword);
}