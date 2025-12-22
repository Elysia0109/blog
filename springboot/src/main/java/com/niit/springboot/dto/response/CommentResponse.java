package com.niit.springboot.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private String username;
    private String avatar;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    private Integer parentId;
    private List<CommentResponse> replies; // 回复列表
}