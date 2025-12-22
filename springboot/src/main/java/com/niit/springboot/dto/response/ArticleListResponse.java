package com.niit.springboot.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleListResponse {
    private Integer id;
    private String title;
    private String category;
    private String summary;
    private String coverImage;
    private String authorName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private LocalDateTime publishTime;
}