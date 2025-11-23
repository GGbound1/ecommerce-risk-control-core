package com.risk.core.model;

import lombok.Data;
import java.util.*;

/**
 * 风险事件数据模型
 */
@Data
public class RiskEvent {
    private String eventId = UUID.randomUUID().toString();
    private String orderId;
    private String userId;
    private Long timestamp = System.currentTimeMillis();
    private List<String> actions = new ArrayList<>();
    private Map<String, Object> context = new HashMap<>();
    private Double amount;
    private String deviceId;
    private String ip;
    
    public boolean hasRisk() {
        return actions != null && !actions.isEmpty();
    }
}