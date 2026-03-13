package com.restaurant.ordering.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PaymentUtil {

    @Value("${wechat.miniapp.mch-id}")
    private String mchId;

    @Value("${wechat.miniapp.mch-key}")
    private String mchKey;

    /**
     * 生成订单号
     */
    public String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String randomStr = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        return "ORDER" + timeStr + randomStr;
    }

    /**
     * 生成支付流水号
     */
    public String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成微信支付参数（模拟）
     */
    public Map<String, String> createWechatPayParams(String orderNo, BigDecimal amount, String openid) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "mock_appid");
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        params.put("package", "prepay_id=mock_prepay_id");
        params.put("signType", "MD5");
        params.put("paySign", "mock_sign");

        // 实际需要调用微信支付统一下单API
        // 这里返回模拟参数
        return params;
    }

    /**
     * 验证微信支付回调签名
     */
    public boolean verifyWechatCallback(Map<String, String> params) {
        // 模拟验证，实际需要验证签名
        return true;
    }

    /**
     * 处理微信支付回调
     */
    public Map<String, String> parseWechatCallback(String xmlData) {
        // 模拟解析XML
        Map<String, String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("result_code", "SUCCESS");
        result.put("out_trade_no", "ORDER20250101000001");
        result.put("transaction_id", "mock_transaction_id");
        result.put("total_fee", "100");
        result.put("time_end", "20250101120000");
        return result;
    }
}