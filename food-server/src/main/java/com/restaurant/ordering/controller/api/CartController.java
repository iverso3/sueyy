package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.request.CartItemRequest;
import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.CartResponse;
import com.restaurant.ordering.model.dto.response.CartResponse.CartItemResponse;
import com.restaurant.ordering.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取当前用户的购物车
     */
    @GetMapping("/get")
    public ApiResponse<CartResponse> getCart(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }
        CartResponse cart = cartService.getCart(userId);
        return ApiResponse.success(cart);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addItemToCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CartItemRequest request) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }
        CartItemResponse cartItem = cartService.addItemToCart(userId, request);
        return ApiResponse.success("添加成功", cartItem);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/items/{itemId}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }
        CartItemResponse cartItem = cartService.updateCartItem(userId, itemId, quantity);
        return ApiResponse.success("更新成功", cartItem);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> removeCartItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long itemId) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }
        cartService.removeCartItem(userId, itemId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public ApiResponse<Void> clearCart(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }
        cartService.clearCart(userId);
        return ApiResponse.success("清空成功", null);
    }
}