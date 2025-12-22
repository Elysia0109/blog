package com.niit.springboot.mapper;

import com.niit.springboot.entity.Comment;
import com.niit.springboot.dto.response.CommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    // 插入评论
    int insert(Comment comment);

    // 根据ID查询
    Comment selectById(Integer id);

    // 根据文章ID查询评论
    List<CommentResponse> selectByArticleId(Integer articleId);

    // 更新评论
    int update(Comment comment);

    // 逻辑删除
    int deleteById(Integer id);

    // 统计文章评论数
    int countByArticleId(Integer articleId);
}