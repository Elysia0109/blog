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
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}