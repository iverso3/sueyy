package com.restaurant.ordering.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    private String encryptedData;
    private String iv;
}