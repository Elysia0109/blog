package com.niit.springboot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {
    // 添加点赞记录
    int insert(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    // 删除点赞记录
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    // 检查是否已点赞
    boolean existsByUserAndArticle(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    // 获取文章点赞数量
    int countByArticle(Integer articleId);
}