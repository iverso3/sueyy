package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.request.CreateOrderRequest;
import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.OrderResponse;
import com.restaurant.ordering.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }

        log.info("创建订单，用户ID: {}, 选中菜品: {}", userId, request.getItemIds());

        OrderResponse order = orderService.createOrder(userId, request);

        return ApiResponse.success(Map.of("orderId", order.getId()));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long orderId) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }

        OrderResponse order = orderService.getOrderById(orderId);
        return ApiResponse.success(order);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // 如果没有用户ID，使用默认测试用户（兼容旧版本）
        if (userId == null) {
            userId = 1L;
        }

        log.info("获取订单列表，用户ID: {}, 角色: {}", userId, userRole);

        List<OrderResponse> orders;
        // 管理员获取所有订单
        if ("ADMIN".equals(userRole)) {
            log.info("管理员模式，获取所有订单");
            orders = orderService.getAllOrders();
        } else {
            orders = orderService.getOrdersByUserId(userId);
        }
        return ApiResponse.success(orders);
    }
}
