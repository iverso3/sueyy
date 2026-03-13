package com.restaurant.ordering.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String imageUrl;
    private Boolean isRecommended;
    private Boolean isHot;
    private Integer stock;
    private Integer sortOrder;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
}