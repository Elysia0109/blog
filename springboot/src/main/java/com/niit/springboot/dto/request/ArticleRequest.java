package com.niit.springboot.dto.request;

import lombok.Data;

@Data
public class ArticleRequest {
    private String title;     // 去掉注解
    private String category;  // 去掉注解
    private String content;   // 去掉注解
    private String summary;
    private String coverImage;
    private Integer isTop;
}