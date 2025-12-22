package com.niit.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;     // 0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}