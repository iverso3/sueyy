package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.request.CreateReviewRequest;
import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.DishReviewResponse;
import com.restaurant.ordering.service.DishReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class DishReviewController {

    private final DishReviewService dishReviewService;

    /**
     * 创建评价
     */
    @PostMapping
    public ApiResponse<DishReviewResponse> createReview(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateReviewRequest request) {
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        DishReviewResponse review = dishReviewService.createReview(userId, request);
        return ApiResponse.success("评价成功", review);
    }

    /**
     * 获取订单项的评价
     */
    @GetMapping("/order-item/{orderItemId}")
    public ApiResponse<DishReviewResponse> getReviewByOrderItemId(@PathVariable Long orderItemId) {
        DishReviewResponse review = dishReviewService.getReviewByOrderItemId(orderItemId);
        return ApiResponse.success(review);
    }

    /**
     * 获取菜品的评价列表
     */
    @GetMapping("/menu-item/{menuItemId}")
    public ApiResponse<List<DishReviewResponse>> getReviewsByMenuItemId(@PathVariable Long menuItemId) {
        List<DishReviewResponse> reviews = dishReviewService.getReviewsByMenuItemId(menuItemId);
        return ApiResponse.success(reviews);
    }

    /**
     * 获取所有评价（管理员）
     */
    @GetMapping
    public ApiResponse<List<DishReviewResponse>> getAllReviews(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            return ApiResponse.error("无权限操作");
        }
        List<DishReviewResponse> reviews = dishReviewService.getAllReviews();
        return ApiResponse.success(reviews);
    }
}
