package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.CreateReviewRequest;
import com.restaurant.ordering.model.dto.response.DishReviewResponse;
import com.restaurant.ordering.model.entity.*;
import com.restaurant.ordering.repository.*;
import com.restaurant.ordering.service.DishReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DishReviewServiceImpl implements DishReviewService {

    private final DishReviewRepository dishReviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DishReviewResponse createReview(Long userId, CreateReviewRequest request) {
        log.info("创建评价，用户ID: {}, 订单项ID: {}", userId, request.getOrderItemId());

        // 获取订单项
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单项不存在"));

        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        // 获取菜品
        MenuItem menuItem = orderItem.getMenuItem();

        // 检查是否已评价
        if (dishReviewRepository.findByOrderItemId(request.getOrderItemId()).isPresent()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "该订单项已评价");
        }

        // 创建评价
        DishReview review = new DishReview();
        review.setOrderItem(orderItem);
        review.setUser(user);
        review.setMenuItem(menuItem);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        DishReview saved = dishReviewRepository.save(review);
        log.info("评价创建成功，评价ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DishReviewResponse getReviewByOrderItemId(Long orderItemId) {
        return dishReviewRepository.findByOrderItemId(orderItemId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishReviewResponse> getReviewsByMenuItemId(Long menuItemId) {
        return dishReviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishReviewResponse> getAllReviews() {
        return dishReviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private DishReviewResponse convertToResponse(DishReview review) {
        DishReviewResponse response = new DishReviewResponse();
        response.setId(review.getId());
        response.setOrderItemId(review.getOrderItem().getId());
        response.setUserId(review.getUser().getId());
        response.setMenuItemId(review.getMenuItem().getId());
        response.setMenuItemName(review.getMenuItem().getName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setIsAnonymous(review.getIsAnonymous());
        response.setCreatedAt(review.getCreatedAt());

        // 处理匿名
        if (review.getIsAnonymous()) {
            response.setUserNickname("匿名用户");
            response.setUserAvatar("");
        } else {
            response.setUserNickname(review.getUser().getNickname() != null ?
                    review.getUser().getNickname() : "用户" + review.getUser().getId());
            response.setUserAvatar(review.getUser().getAvatarUrl() != null ?
                    review.getUser().getAvatarUrl() : "");
        }

        return response;
    }
}
