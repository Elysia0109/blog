package com.niit.springboot.mapper;

import com.niit.springboot.entity.Article;
import com.niit.springboot.dto.response.ArticleListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    // 插入文章
    int insert(Article article);

    // 根据ID查询
    Article selectById(Integer id);

    // 更新文章
    int update(Article article);

    // 逻辑删除
    int deleteById(Integer id);

    // 增加阅读量
    int incrementViewCount(Integer id);

    // 更新点赞数
    int updateLikeCount(@Param("id") Integer id, @Param("increment") Integer increment);

    // 获取文章列表（分页）
    List<ArticleListResponse> selectArticleList(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 根据分类获取文章
    List<ArticleListResponse> selectArticlesByCategory(
            @Param("category") String category,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 获取用户文章
    List<ArticleListResponse> selectArticlesByAuthor(
            @Param("authorId") Integer authorId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 根据ID列表获取文章
    List<ArticleListResponse> selectArticlesByIds(
            @Param("ids") List<Integer> ids,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 搜索文章
    List<ArticleListResponse> searchArticles(
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 置顶/取消置顶
    int toggleTop(@Param("id") Integer id, @Param("isTop") Integer isTop);

    // 统计总数
    int countArticles();

    // 统计分类文章数
    int countArticlesByCategory(String category);

    // 统计用户文章数
    int countArticlesByAuthor(Integer authorId);

    // 统计搜索结果数量
    int countSearchArticles(String keyword);
}