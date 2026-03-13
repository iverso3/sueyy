package com.restaurant.ordering.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误
    SUCCESS(200, "成功"),
    SYSTEM_ERROR(500, "系统错误"),
    VALIDATION_ERROR(400, "参数验证失败"),

    // 认证错误
    AUTH_FAILED(1001, "认证失败"),
    TOKEN_EXPIRED(1002, "Token已过期"),
    TOKEN_INVALID(1003, "Token无效"),
    PERMISSION_DENIED(1004, "权限不足"),

    // 业务错误
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_BANNED(2009, "用户已被禁用"),
    MENU_ITEM_NOT_FOUND(2002, "菜品不存在"),
    CATEGORY_NOT_FOUND(2010, "分类不存在"),
    MENU_ITEM_OUT_OF_STOCK(2003, "菜品库存不足"),
    CART_EMPTY(2004, "购物车为空"),
    CART_ITEM_NOT_FOUND(2008, "购物车商品不存在"),
    ORDER_NOT_FOUND(2005, "订单不存在"),
    ORDER_STATUS_INVALID(2006, "订单状态无效"),
    PAYMENT_FAILED(2007, "支付失败"),

    // 微信相关错误
    WECHAT_LOGIN_FAILED(3001, "微信登录失败"),
    WECHAT_PAY_FAILED(3002, "微信支付失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}