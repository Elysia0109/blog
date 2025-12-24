package com.niit.springboot.entity;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BrowseRecord {
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private LocalDateTime browseTime;
    private Integer duration;
}