package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理服务实现
 * 使用内存存储会话，生产环境可考虑使用 Redis
 */
@Service
@Slf4j
public class SessionServiceImpl implements SessionService {

    /**
     * 会话存储：token -> SessionInfo
     */
    private final ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    /**
     * JWT 过期时间（毫秒），从配置读取
     */
    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationInMs;

    @Override
    public void createSession(String token, Long userId) {
        SessionInfo info = new SessionInfo(userId, LocalDateTime.now());
        sessions.put(token, info);
        log.info("创建会话: userId={}, token前8位={}", userId, token.substring(0, Math.min(8, token.length())));
    }

    @Override
    public boolean validateSession(String token) {
        SessionInfo info = sessions.get(token);
        if (info == null) {
            log.debug("会话不存在: token前8位={}", token.substring(0, Math.min(8, token.length())));
            return false;
        }

        // 检查是否过期
        LocalDateTime expireTime = info.getLoginTime().plusNanos(jwtExpirationInMs * 1_000_000);
        if (LocalDateTime.now().isAfter(expireTime)) {
            log.info("会话已过期: userId={}", info.getUserId());
            sessions.remove(token);
            return false;
        }

        return true;
    }

    @Override
    public void destroySession(String token) {
        SessionInfo removed = sessions.remove(token);
        if (removed != null) {
            log.info("销毁会话: userId={}, token前8位={}", removed.getUserId(), token.substring(0, Math.min(8, token.length())));
        }
    }

    @Override
    public Long getUserIdFromSession(String token) {
        SessionInfo info = sessions.get(token);
        if (info == null) {
            return null;
        }
        // 先验证是否过期
        if (!validateSession(token)) {
            return null;
        }
        return info.getUserId();
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟清理一次
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        long expireTimeNanos = jwtExpirationInMs * 1_000_000;

        sessions.entrySet().removeIf(entry -> {
            SessionInfo info = entry.getValue();
            LocalDateTime expireTime = info.getLoginTime().plusNanos(expireTimeNanos);
            return now.isAfter(expireTime);
        });

        if (log.isDebugEnabled()) {
            log.debug("清理过期会话，当前会话数: {}", sessions.size());
        }
    }

    /**
     * 会话信息内部类
     */
    private static class SessionInfo {
        private final Long userId;
        private final LocalDateTime loginTime;

        public SessionInfo(Long userId, LocalDateTime loginTime) {
            this.userId = userId;
            this.loginTime = loginTime;
        }

        public Long getUserId() {
            return userId;
        }

        public LocalDateTime getLoginTime() {
            return loginTime;
        }
    }
}
