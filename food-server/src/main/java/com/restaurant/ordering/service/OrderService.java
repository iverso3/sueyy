package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.CreateOrderRequest;
import com.restaurant.ordering.model.dto.response.OrderResponse;
import com.restaurant.ordering.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

    void deleteOrder(Long orderId, Long userId, String userRole);

    OrderResponse deleteOrderItem(Long orderId, Long itemId, Long userId, String userRole);
}
