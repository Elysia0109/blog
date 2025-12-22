package com.niit.springboot.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Integer id;
    private String account;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private Integer status;
}