package com.restaurant.ordering.model.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String role;
}