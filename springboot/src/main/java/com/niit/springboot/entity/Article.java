package com.niit.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Integer id;
    private String title;
    private String category;
    private String content;
    private String summary;
    private String coverImage;
    private Integer authorId;
    private String authorName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer status;     // 0-草稿，1-已发布，2-已删除
    private Integer isTop;      // 0-否，1-是
    private Integer isDeleted;  // 0-正常，1-删除
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}