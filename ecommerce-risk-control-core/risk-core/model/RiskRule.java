package com.risk.core.model;

import lombok.Data;

/**
 * 风险规则数据模型
 */
@Data
public class RiskRule {
    private String ruleId;
    private String ruleName;
    private String ruleType;
    private Integer threshold;
    private Integer timeWindow;
    private String action;
}