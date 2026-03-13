package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.LoginRequest;
import com.restaurant.ordering.model.dto.response.LoginResponse;
import com.restaurant.ordering.model.entity.User;

public interface AuthService {

    LoginResponse wechatLogin(LoginRequest request);

    User getUserFromToken(String token);

    void logout(String token);

    /**
     * 验证会话是否有效
     * @param token 用户token
     * @return 会话是否有效
     */
    boolean validateSession(String token);

    /**
     * 获取当前登录用户
     * @param token 用户token
     * @return 用户信息，如果会话无效返回null
     */
    User getCurrentUser(String token);
}