package com.restaurant.ordering.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DishReviewResponse {
    private Long id;
    private Long orderItemId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long menuItemId;
    private String menuItemName;
    private Integer rating;
    private String comment;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
}
