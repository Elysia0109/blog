package com.niit.springboot.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleDetailResponse {
    private Integer id;
    private String title;
    private String category;
    private String content;
    private String summary;
    private String coverImage;
    private Integer authorId;
    private String authorName;
    private String authorAvatar; // 作者头像
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Boolean isTop;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private Boolean isFavorite; // 是否收藏
    private Boolean isLiked;    // 是否点赞
}