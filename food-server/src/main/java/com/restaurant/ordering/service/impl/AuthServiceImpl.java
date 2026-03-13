package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.LoginRequest;
import com.restaurant.ordering.model.dto.response.LoginResponse;
import com.restaurant.ordering.model.dto.response.UserInfoResponse;
import com.restaurant.ordering.model.entity.User;
import com.restaurant.ordering.model.enums.UserRole;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.service.AuthService;
import com.restaurant.ordering.service.SessionService;
import com.restaurant.ordering.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse wechatLogin(LoginRequest request) {
        try {
            // TODO: 后续对接真实微信登录API
            // 1. 使用 code 调用微信登录凭证校验接口获取 session_key 和 openid
            // 2. 根据 openid 查找或创建用户
            // 示例代码：
            // WeChatUtils weChatUtils = new WeChatUtils();
            // WeChatSession session = weChatUtils.getSession(request.getCode());
            // String openid = session.getOpenid();

            // 模拟微信登录（当前使用）
            // String openid = "mock_openid_" + System.currentTimeMillis();
            String openid = "admin";

            // 查找或创建用户
            User user = userRepository.findByOpenid(openid)
                    .orElseGet(() -> createNewUser(openid));

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus().name().equals("BANNED")) {
                throw new BusinessException(ErrorCode.USER_BANNED);
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            // 生成 JWT token（包含用户ID）
            String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

            // 创建会话记录
            sessionService.createSession(token, user.getId());

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setExpiresIn((int) jwtTokenProvider.getExpirationInSeconds());

            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setOpenid(user.getOpenid());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            userInfo.setPhone(user.getPhone());
            userInfo.setRole(user.getRole().name());

            response.setUserInfo(userInfo);

            log.info("用户登录成功: userId={}, openid={}", user.getId(), openid);
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED);
        }
    }

    @Override
    public User getUserFromToken(String token) {
        return getCurrentUser(token);
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 销毁会话
            sessionService.destroySession(token);
            log.info("用户登出: token前8位={}", token.substring(0, Math.min(8, token.length())));
        }
    }

    @Override
    public boolean validateSession(String token) {
        return sessionService.validateSession(token);
    }

    @Override
    public User getCurrentUser(String token) {
        // 先验证会话
        if (!sessionService.validateSession(token)) {
            return null;
        }

        // 从 token 获取用户ID
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }

        // 查找用户
        return userRepository.findById(userId).orElse(null);
    }

    private User createNewUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname("微信用户_" + openid.substring(0, 8));
        user.setRole(UserRole.USER);
        user.setAvatarUrl("https://example.com/default-avatar.png");
        return userRepository.save(user);
    }
}
