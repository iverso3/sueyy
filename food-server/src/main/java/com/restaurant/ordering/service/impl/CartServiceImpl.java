package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.CartItemRequest;
import com.restaurant.ordering.model.dto.response.CartResponse;
import com.restaurant.ordering.model.entity.*;
import com.restaurant.ordering.model.dto.response.CartResponse.CartItemResponse;
import com.restaurant.ordering.repository.CartItemRepository;
import com.restaurant.ordering.repository.CartRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        log.debug("获取用户购物车，userId: {}", userId);
        try {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                List<CartItem> cartItems = cartItemRepository.findByCart(cart);
                log.debug("找到购物车: {}, 商品数量: {}", cart.getId(), cartItems.size());
                return buildCartResponse(cart, cartItems);
            } else {
                log.debug("用户购物车不存在，返回空购物车");
                // 返回空购物车响应
                CartResponse emptyResponse = new CartResponse();
                emptyResponse.setUserId(userId);
                emptyResponse.setItems(java.util.Collections.emptyList());
                emptyResponse.setTotalQuantity(0);
                emptyResponse.setTotalPrice(BigDecimal.ZERO);
                return emptyResponse;
            }
        } catch (Exception e) {
            log.error("获取购物车失败，userId: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CartItemResponse addItemToCart(Long userId, CartItemRequest request) {
        // 验证请求参数
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));

        // 检查商品库存
        if (menuItem.getStock() != null && menuItem.getStock() != -1 && menuItem.getStock() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.MENU_ITEM_OUT_OF_STOCK);
        }

        // 检查商品状态
        if (menuItem.getIsActive() == null || !menuItem.getIsActive()) {
            throw new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        // 检查商品是否已存在购物车中
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndMenuItem(cart, menuItem);

        if (existingItemOpt.isPresent()) {
            // 已存在，更新数量
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            return updateCartItemQuantityAndReturn(existingItem, newQuantity, cart);
        } else {
            // 新商品，添加到购物车
            CartItem savedItem = createNewCartItem(cart, menuItem, request.getQuantity());
            return convertToCartItemResponse(savedItem);
        }
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 验证购物车项目属于当前用户的购物车
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        if (quantity <= 0) {
            // 数量为0或负数，删除商品
            cartItemRepository.delete(cartItem);
            recalculateCart(cart);
            return null;
        }

        return updateCartItemQuantityAndReturn(cartItem, quantity, cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 验证购物车项目属于当前用户的购物车
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cartItemRepository.delete(cartItem);
        recalculateCart(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart(cart);
        recalculateCart(cart);
    }

    /**
     * 获取或创建购物车
     */
    private Cart getOrCreateCart(Long userId) {
        log.debug("获取或创建购物车，用户ID: {}", userId);
        try {
            Optional<Cart> existingCart = cartRepository.findByUserId(userId);

            if (existingCart.isPresent()) {
                log.debug("找到现有购物车: {}", existingCart.get().getId());
                return existingCart.get();
            } else {
                log.debug("未找到购物车，创建新购物车");
                return createCart(userId);
            }
        } catch (Exception e) {
            log.error("获取或创建购物车失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 创建新购物车
     */
    private Cart createCart(Long userId) {
        log.debug("尝试为用户创建购物车: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("用户不存在，无法创建购物车: {}", userId);
            return new BusinessException(ErrorCode.USER_NOT_FOUND);
        });

        log.debug("找到用户: {}，创建购物车", userId);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setItemCount(0);

        try {
            return cartRepository.save(cart);
        } catch (Exception e) {
            log.error("创建购物车失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 创建新的购物车商品
     */
    private CartItem createNewCartItem(Cart cart, MenuItem menuItem, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(menuItem.getPrice());
        cartItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));

        CartItem savedItem = cartItemRepository.save(cartItem);
        recalculateCart(cart);

        return savedItem;
    }

    /**
     * 更新购物车商品数量并返回DTO
     */
    private CartItemResponse updateCartItemQuantityAndReturn(CartItem cartItem, Integer quantity, Cart cart) {
        // 检查数量是否有效
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        // 检查商品库存
        MenuItem menuItem = cartItem.getMenuItem();
        if (menuItem.getStock() != null && menuItem.getStock() != -1 && menuItem.getStock() < quantity) {
            throw new BusinessException(ErrorCode.MENU_ITEM_OUT_OF_STOCK);
        }

        // 检查商品状态
        if (menuItem.getIsActive() == null || !menuItem.getIsActive()) {
            throw new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(quantity)));

        CartItem updatedItem = cartItemRepository.save(cartItem);
        recalculateCart(cart);

        return convertToCartItemResponse(updatedItem);
    }

    /**
     * 重新计算购物车总价和商品数量
     */
    private void recalculateCart(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = cartItems.size();

        cart.setTotalPrice(totalPrice);
        cart.setItemCount(itemCount);
        cartRepository.save(cart);
    }

    /**
     * 构建购物车响应
     */
    private CartResponse buildCartResponse(Cart cart, List<CartItem> cartItems) {
        CartResponse response = new CartResponse();
        response.setUserId(cart.getUser().getId());

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setTotalQuantity(cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum());
        response.setTotalPrice(cart.getTotalPrice());

        return response;
    }

    /**
     * 转换购物车商品为响应对象
     */
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setMenuItemId(cartItem.getMenuItem().getId());
        response.setMenuItemName(cartItem.getMenuItem().getName());
        response.setPrice(cartItem.getPrice());
        response.setQuantity(cartItem.getQuantity());
        response.setSubtotal(cartItem.getSubtotal());
        return response;
    }
}