package com.niit.springboot.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    private Integer articleId;  // 去掉注解
    private String content;     // 去掉注解
    private Integer parentId;
}