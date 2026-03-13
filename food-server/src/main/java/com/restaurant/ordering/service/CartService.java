package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.CartItemRequest;
import com.restaurant.ordering.model.dto.response.CartResponse;
import com.restaurant.ordering.model.dto.response.CartResponse.CartItemResponse;
import com.restaurant.ordering.model.entity.CartItem;

import java.util.List;

public interface CartService {

    CartResponse getCart(Long userId);

    CartItemResponse addItemToCart(Long userId, CartItemRequest request);

    CartItemResponse updateCartItem(Long userId, Long itemId, Integer quantity);

    void removeCartItem(Long userId, Long itemId);

    void clearCart(Long userId);
}