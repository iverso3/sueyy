package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.CreateOrderRequest;
import com.restaurant.ordering.model.dto.response.OrderResponse;
import com.restaurant.ordering.model.entity.*;
import com.restaurant.ordering.repository.*;
import com.restaurant.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("创建订单，用户ID: {}, 选中菜品: {}", userId, request.getItemIds());

        // 1. 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 获取购物车
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        // 3. 获取选中的购物车项
        List<CartItem> selectedCartItems = cartItemRepository.findAllById(request.getItemIds())
                .stream()
                .filter(item -> item.getCart().getId().equals(cart.getId()))
                .collect(Collectors.toList());

        log.info("找到选中的购物车项数量: {}", selectedCartItems.size());

        if (selectedCartItems.isEmpty()) {
            log.warn("未找到选中的购物车项，itemIds: {}", request.getItemIds());
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 4. 生成订单号
        String orderNo = generateOrderNo();

        // 5. 计算订单金额
        BigDecimal totalAmount = selectedCartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setActualAmount(totalAmount);
        order.setStatus(com.restaurant.ordering.model.enums.OrderStatus.PENDING);
        order.setPaymentMethod(com.restaurant.ordering.model.enums.PaymentMethod.WECHAT);
        order.setPaymentStatus(com.restaurant.ordering.model.enums.PaymentStatus.UNPAID);
        // 处理配送时间
        LocalDateTime pickupTime = parseDeliveryTime(request.getDeliveryTime());
        order.setPickupTime(pickupTime);
        order.setRemark(request.getRemark());

        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功，订单号: {}", orderNo);

        // 7. 创建订单项
        for (CartItem cartItem : selectedCartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setMenuItemName(cartItem.getMenuItem().getName());
            orderItem.setMenuItemPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getSubtotal());
            orderItemRepository.save(orderItem);
        }

        // 8. 删除购物车中已下单的菜品
        List<Long> itemIds = selectedCartItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toList());
        cartItemRepository.deleteAllById(itemIds);
        cartItemRepository.flush(); // 立即刷新以确保删除操作生效

        log.info("已删除购物车中的菜品，itemIds: {}", itemIds);

        // 9. 重新计算购物车
        recalculateCart(cart);

        // 10. 返回订单响应
        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("获取所有订单列表（管理员）");
        List<Order> orders = orderRepository.findAll();
        // 按创建时间倒序排列
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * 解析配送时间
     */
    private LocalDateTime parseDeliveryTime(String deliveryTime) {
        if (deliveryTime == null || deliveryTime.isEmpty()) {
            return LocalDateTime.now().plusMinutes(30);
        }

        if ("尽快送达".equals(deliveryTime)) {
            return LocalDateTime.now().plusMinutes(30);
        } else if ("30分钟内".equals(deliveryTime)) {
            return LocalDateTime.now().plusMinutes(30);
        } else if ("1小时内".equals(deliveryTime)) {
            return LocalDateTime.now().plusHours(1);
        }
        // 默认30分钟
        return LocalDateTime.now().plusMinutes(30);
    }

    /**
     * 生成唯一订单号
     */
    private String generateOrderNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    /**
     * 重新计算购物车
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
     * 构建订单响应
     */
    private OrderResponse buildOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setActualAmount(order.getActualAmount());
        response.setStatus(order.getStatus().name());
        response.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        response.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        response.setPickupTime(order.getPickupTime());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());

        // 获取订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::buildOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    /**
     * 构建订单项响应
     */
    private OrderResponse.OrderItemResponse buildOrderItemResponse(OrderItem orderItem) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItemName());
        response.setImageUrl(orderItem.getMenuItem().getImageUrl());
        response.setPrice(orderItem.getMenuItemPrice());
        response.setQuantity(orderItem.getQuantity());
        response.setSubtotal(orderItem.getSubtotal());
        return response;
    }
}
