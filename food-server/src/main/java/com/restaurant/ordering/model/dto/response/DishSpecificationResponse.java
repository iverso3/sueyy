package com.restaurant.ordering.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSpecificationResponse {
    private Long id;
    private String name;
    private BigDecimal priceAdjustment;
    private Boolean isDefault;
}
