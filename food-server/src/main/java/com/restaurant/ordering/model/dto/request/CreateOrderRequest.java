package com.restaurant.ordering.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "请选择要下单的菜品")
    private List<Long> itemIds;

    @NotNull(message = "配送时间不能为空")
    private String deliveryTime;

    private String remark;
}
