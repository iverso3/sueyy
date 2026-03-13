package com.restaurant.ordering.service;

/**
 * 会话管理服务接口
 */
public interface SessionService {

    /**
     * 创建会话
     * @param token 用户token
     * @param userId 用户ID
     */
    void createSession(String token, Long userId);

    /**
     * 验证会话是否有效
     * @param token 用户token
     * @return 会话是否有效
     */
    boolean validateSession(String token);

    /**
     * 销毁会话
     * @param token 用户token
     */
    void destroySession(String token);

    /**
     * 从 token 获取用户ID
     * @param token 用户token
     * @return 用户ID，如果无效返回null
     */
    Long getUserIdFromSession(String token);

    /**
     * 清理所有过期会话
     */
    void cleanupExpiredSessions();
}
