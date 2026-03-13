package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.request.LoginRequest;
import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.LoginResponse;
import com.restaurant.ordering.model.entity.User;
import com.restaurant.ordering.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.wechatLogin(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token != null) {
            authService.logout(token);
            log.info("用户登出成功");
        }
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 验证会话是否有效
     */
    @GetMapping("/validate")
    public ApiResponse<Map<String, Object>> validateSession(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        Map<String, Object> result = new HashMap<>();

        if (token == null) {
            result.put("valid", false);
            result.put("message", "未提供token");
            return ApiResponse.success(result);
        }

        boolean isValid = authService.validateSession(token);
        result.put("valid", isValid);

        if (isValid) {
            User user = authService.getCurrentUser(token);
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatarUrl", user.getAvatarUrl());
                result.put("user", userInfo);
            }
        } else {
            result.put("message", "会话已过期，请重新登录");
        }

        return ApiResponse.success(result);
    }

    /**
     * 从 Authorization 头提取 Token
     */
    private String extractToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}