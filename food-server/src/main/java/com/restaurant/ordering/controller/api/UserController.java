package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.common.ApiResponse;
import com.restaurant.ordering.model.entity.User;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ApiResponse<User> getUserInfo(@RequestAttribute("userId") Long userId) {
        return userRepository.findById(userId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("用户不存在"));
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> params) {

        return userRepository.findById(userId)
                .map(user -> {
                    if (params.containsKey("nickname")) {
                        user.setNickname(params.get("nickname"));
                    }
                    if (params.containsKey("avatarUrl")) {
                        user.setAvatarUrl(params.get("avatarUrl"));
                    }
                    if (params.containsKey("phone")) {
                        user.setPhone(params.get("phone"));
                    }

                    User saved = userRepository.save(user);
                    log.info("用户资料更新成功: userId={}", userId);
                    return ApiResponse.success(saved);
                })
                .orElse(ApiResponse.error("用户不存在"));
    }
}
