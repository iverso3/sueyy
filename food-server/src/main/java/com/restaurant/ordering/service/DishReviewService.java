package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.CreateReviewRequest;
import com.restaurant.ordering.model.dto.response.DishReviewResponse;

import java.util.List;

public interface DishReviewService {

    DishReviewResponse createReview(Long userId, CreateReviewRequest request);

    DishReviewResponse getReviewByOrderItemId(Long orderItemId);

    List<DishReviewResponse> getReviewsByMenuItemId(Long menuItemId);

    List<DishReviewResponse> getAllReviews();
}
