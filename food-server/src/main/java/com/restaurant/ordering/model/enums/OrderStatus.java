package com.restaurant.ordering.model.enums;

public enum OrderStatus {
    PENDING,    // 待支付
    PAID,       // 已支付
    PREPARING,  // 制作中
    READY,      // 待取餐
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}