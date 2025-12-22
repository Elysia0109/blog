package com.niit.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String account;
    private String username;
    private String nickname;
    private String password;
    private String avatar;
    private String email;
    private Integer status;
    private Integer isDeleted;  // 逻辑删除：0-正常，1-删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 注意：标准MyBatis没有自动填充功能
    // 需要在SQL中处理时间字段
}