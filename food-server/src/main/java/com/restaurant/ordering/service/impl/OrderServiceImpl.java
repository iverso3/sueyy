package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.CreateOrderRequest;
import com.restaurant.ordering.model.dto.response.OrderResponse;
import com.restaurant.ordering.model.entity.*;
import com.restaurant.ordering.model.enums.OrderStatus;
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
    private final DishReviewRepository dishReviewRepository;

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
        order.setStatus(com.restaurant.ordering.model.enums.OrderStatus.PLACED);
        order.setPaymentMethod(com.restaurant.ordering.model.enums.PaymentMethod.WECHAT);
        order.setPaymentStatus(com.restaurant.ordering.model.enums.PaymentStatus.UNPAID);
        // 处理取餐时间
        LocalDateTime pickupTime = parseDeliveryTime(request.getPickupTime());
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

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("修改订单状态，订单ID: {}, 新状态: {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long userId, String userRole) {
        log.info("删除订单，订单ID: {}, 用户ID: {}, 角色: {}", orderId, userId, userRole);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 检查权限：管理员可以删除任意订单，用户只能删除自己的当天订单
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOwnOrder = order.getUser().getId().equals(userId);

        if (!isAdmin && !isOwnOrder) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权删除此订单");
        }

        // 如果是用户删除自己的订单，检查是否是当天订单
        if (!isAdmin && isOwnOrder) {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
            if (order.getCreatedAt().isBefore(startOfDay)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能删除当天的订单");
            }
        }

        // 删除订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        // 先删除关联的评价记录
        for (OrderItem item : orderItems) {
            dishReviewRepository.findByOrderItemId(item.getId())
                    .ifPresent(dishReviewRepository::delete);
        }
        orderItemRepository.deleteAll(orderItems);

        // 删除订单
        orderRepository.delete(order);
        log.info("订单删除成功，订单ID: {}", orderId);
    }

    @Override
    @Transactional
    public OrderResponse deleteOrderItem(Long orderId, Long itemId, Long userId, String userRole) {
        log.info("删除订单菜品，订单ID: {}, 菜品ID: {}, 用户ID: {}, 角色: {}", orderId, itemId, userId, userRole);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 检查权限
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOwnOrder = order.getUser().getId().equals(userId);

        if (!isAdmin && !isOwnOrder) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权删除此订单的菜品");
        }

        // 如果是用户操作，检查是否是当天订单
        if (!isAdmin && isOwnOrder) {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
            if (order.getCreatedAt().isBefore(startOfDay)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能删除当天订单的菜品");
            }
        }

        // 查找订单项
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND, "菜品不存在"));

        // 检查订单项是否属于该订单
        if (!orderItem.getOrder().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "菜品不属于该订单");
        }

        // 减去金额
        BigDecimal subtotal = orderItem.getSubtotal();
        order.setTotalAmount(order.getTotalAmount().subtract(subtotal));
        order.setActualAmount(order.getActualAmount().subtract(subtotal));

        // 先删除关联的评价记录
        dishReviewRepository.findByOrderItemId(orderItem.getId())
                .ifPresent(dishReviewRepository::delete);

        // 删除订单项
        orderItemRepository.delete(orderItem);

        // 保存订单
        Order savedOrder = orderRepository.save(order);
        log.info("订单菜品删除成功，菜品ID: {}", itemId);

        return buildOrderResponse(savedOrder);
    }

    /**
     * 解析取餐时间
     */
    private LocalDateTime parseDeliveryTime(String pickupTime) {
        if (pickupTime == null || pickupTime.isEmpty()) {
            return LocalDateTime.now().plusMinutes(30);
        }

        try {
            // 尝试解析 yyyy-MM-dd HH:mm:ss 格式
            return LocalDateTime.parse(pickupTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("解析取餐时间失败: {}, 使用默认时间", pickupTime);
            // 如果解析失败，尝试其他常见格式
            try {
                return LocalDateTime.parse(pickupTime);
            } catch (Exception e2) {
                return LocalDateTime.now().plusMinutes(30);
            }
        }
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
        response.setUserId(order.getUser().getId());
        response.setUserNickname(order.getUser().getNickname() != null ? order.getUser().getNickname() : order.getUser().getOpenid());
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

        // 获取评价信息
        dishReviewRepository.findByOrderItemId(orderItem.getId()).ifPresent(review -> {
            response.setHasReviewed(true);
            response.setRating(review.getRating());
            response.setComment(review.getComment());
        });

        return response;
    }
}
