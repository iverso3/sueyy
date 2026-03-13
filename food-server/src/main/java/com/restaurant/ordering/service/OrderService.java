package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.CreateOrderRequest;
import com.restaurant.ordering.model.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getAllOrders();
}
