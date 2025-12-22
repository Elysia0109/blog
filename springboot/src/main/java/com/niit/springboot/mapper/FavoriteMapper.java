package com.niit.springboot.mapper;

import com.niit.springboot.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    // 添加收藏
    int insert(Favorite favorite);

    // 删除收藏
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    // 检查是否已收藏
    boolean existsByUserAndArticle(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    // 获取用户收藏的文章ID列表
    List<Integer> selectArticleIdsByUser(Integer userId);

    // 获取文章的收藏数量
    int countByArticle(Integer articleId);

    // 获取用户的收藏数量
    int countByUser(Integer userId);
}
