package com.restaurant.ordering.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequest {

    private Long categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String imageUrl;

    private Integer stock;

    private Boolean isRecommended = false;

    private Boolean isHot = false;

    private Integer sortOrder = 0;

    private Boolean isActive = true;
}
