package com.niit.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private Integer parentId;   // 0-顶级评论
    private String content;
    private Integer likeCount;
    private Integer status;     // 0-隐藏，1-显示
    private Integer isDeleted;  // 0-正常，1-删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
