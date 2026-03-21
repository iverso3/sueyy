package com.restaurant.ordering.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSpecificationRequest {

    @NotNull(message = "菜品ID不能为空")
    private Long menuItemId;

    @NotBlank(message = "规格名称不能为空")
    @Pattern(regexp = "^.{1,50}$", message = "规格名称长度不能超过50个字符")
    private String name;

    @PositiveOrZero(message = "价格调整不能为负数")
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    private Boolean isDefault = false;

    private Integer sortOrder = 0;
}
