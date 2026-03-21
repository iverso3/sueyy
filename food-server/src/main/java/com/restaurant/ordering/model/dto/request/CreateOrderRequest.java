package com.restaurant.ordering.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "请选择要下单的菜品")
    private List<Long> itemIds;

    private String pickupTime;

    private String remark;
}
