package com.restaurant.ordering.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class WeChatUtil {

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 微信登录，通过code获取openid和session_key
     */
    public Map<String, String> codeToSession(String code) {
        try {
            // 模拟微信API响应
            Map<String, String> result = new HashMap<>();
            result.put("openid", "mock_openid_" + System.currentTimeMillis());
            result.put("session_key", "mock_session_key");
            result.put("unionid", "mock_unionid");
            return result;

            // 实际代码（需要取消注释并配置正确的appid和secret）：
            /*
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, secret, code
            );

            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(response, Map.class);
            */
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new RuntimeException("微信登录失败");
        }
    }

    /**
     * 解密微信加密数据
     */
    public Map<String, String> decryptData(String encryptedData, String sessionKey, String iv) {
        try {
            // 模拟解密，实际需要实现AES解密
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("nickName", "微信用户");
            userInfo.put("avatarUrl", "https://example.com/avatar.png");
            userInfo.put("gender", "0");
            userInfo.put("city", "");
            userInfo.put("province", "");
            userInfo.put("country", "");
            return userInfo;
        } catch (Exception e) {
            log.error("解密微信数据失败", e);
            throw new RuntimeException("解密微信数据失败");
        }
    }
}