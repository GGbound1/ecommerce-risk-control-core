package com.risk.core.model;

import lombok.Data;
import java.util.Map;

/**
 * 订单事件数据模型
 */
@Data
public class OrderEvent {
    private String orderId;
    private String userId;
    private Long timestamp;
    private Double amount;
    private String ip;
    private String deviceId;
    private String paymentMethod;
    private Map<String, String> items;
}