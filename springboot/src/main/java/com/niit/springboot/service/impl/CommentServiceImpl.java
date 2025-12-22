package com.niit.springboot.service.impl;

import com.niit.springboot.dto.request.CommentRequest;
import com.niit.springboot.dto.response.CommentResponse;
import com.niit.springboot.entity.Article;
import com.niit.springboot.entity.Comment;
import com.niit.springboot.mapper.ArticleMapper;
import com.niit.springboot.mapper.CommentMapper;
import com.niit.springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public Integer addComment(CommentRequest request, Integer userId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }

        // 检查父评论（如果存在）
        if (request.getParentId() != null && request.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getIsDeleted() == 1) {
                throw new RuntimeException("父评论不存在");
            }
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1); // 显示

        commentMapper.insert(comment);

        // 更新文章评论数
        // 这里需要ArticleMapper有更新评论数的方法，可以后续添加

        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(Integer id, Integer userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查权限：只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评论");
        }

        commentMapper.deleteById(id);
    }

    @Override
    public List<CommentResponse> getCommentsByArticleId(Integer articleId) {
        List<CommentResponse> comments = commentMapper.selectByArticleId(articleId);

        // 组织层级结构（将回复嵌套到父评论下）
        return organizeComments(comments);
    }

    private List<CommentResponse> organizeComments(List<CommentResponse> allComments) {
        // 创建ID到评论的映射
        Map<Integer, CommentResponse> commentMap = allComments.stream()
                .collect(Collectors.toMap(CommentResponse::getId, c -> c));

        // 找出顶级评论（parentId = 0）
        List<CommentResponse> topLevelComments = allComments.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        // 将回复添加到对应父评论的replies列表中
        for (CommentResponse comment : allComments) {
            if (comment.getParentId() != 0) {
                CommentResponse parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new java.util.ArrayList<>());
                    }
                    parent.getReplies().add(comment);
                }
            }
        }

        return topLevelComments;
    }

    @Override
    public Integer countCommentsByArticleId(Integer articleId) {
        return commentMapper.countByArticleId(articleId);
    }
}