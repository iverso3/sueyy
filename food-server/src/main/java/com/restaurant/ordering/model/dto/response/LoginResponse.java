package com.restaurant.ordering.model.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfoResponse userInfo;
    private Integer expiresIn;  // 过期时间（秒）
}