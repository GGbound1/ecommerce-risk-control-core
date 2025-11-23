package com.risk.core.engine;

import com.risk.core.model.OrderEvent;
import com.risk.core.model.RiskEvent;
import com.risk.core.model.RiskRule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import com.alibaba.fastjson.JSON;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 高性能规则引擎核心
 * 支持并行规则评估和动态规则加载
 */
public class RuleEngine {
    private final JedisPool jedisPool;
    private final OnlineLearningModel onlineModel;
    private final AtomicReference<Map<String, RiskRule>> rulesMapRef;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);

    // Redis键常量
    private static final String RULES_HASH_KEY = "risk:rules:hash";
    private static final String ACTIVE_RULES_SET_KEY = "risk:rules:active";

    public RuleEngine(JedisPool jedisPool, OnlineLearningModel onlineModel) {
        this.jedisPool = jedisPool;
        this.onlineModel = onlineModel;
        this.rulesMapRef = new AtomicReference<>(Collections.emptyMap());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.executor = Executors.newFixedThreadPool(16);
        
        startRuleLoader();
    }

    /**
     * 异步评估订单风险
     */
    public CompletableFuture<RiskEvent> evaluateAsync(OrderEvent order) {
        RiskEvent event = new RiskEvent();
        event.setOrderId(order.getOrderId());
        event.setActions(new ArrayList<>());
        event.setContext(new HashMap<>());

        Map<String, RiskRule> currentRules = rulesMapRef.get();

        List<CompletableFuture<RuleResult>> futures = currentRules.values().stream()
                .map(rule -> CompletableFuture.supplyAsync(() -> evaluateRule(rule, order), executor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(v -> {
            for (CompletableFuture<RuleResult> future : futures) {
                RuleResult result = future.join();
                if (result != null && result.isTriggered()) {
                    event.getActions().add(result.getAction());
                    event.getContext().put(result.getRuleId(), result.getEvidence());
                    onlineModel.collect(order, result);
                }
            }
            return event;
        });
    }

    private RuleResult evaluateRule(RiskRule rule, OrderEvent order) {
        RuleResult result = new RuleResult();
        
        // 简化的规则评估逻辑
        if ("AMOUNT".equals(rule.getRuleType()) && order.getAmount() > rule.getThreshold()) {
            result.setTriggered(true);
            result.setAction(rule.getAction());
            result.setRuleId(rule.getRuleId());
            result.setEvidence(Collections.singletonMap("amount", order.getAmount()));
        } else if ("FREQUENCY".equals(rule.getRuleType())) {
            // 频率检测逻辑
            result.setTriggered(false);
        }
        
        return result;
    }

    private void startRuleLoader() {
        scheduler.scheduleAtFixedRate(() -> loadRules(), 5, 5, TimeUnit.MINUTES);
    }

    private synchronized void loadRules() {
        try (Jedis jedis = jedisPool.getResource()) {
            String luaScript =
                    "local active_rules = redis.call('SMEMBERS', KEYS[1]); " +
                    "if #active_rules == 0 then return {} end; " +
                    "local rule_data = redis.call('HMGET', KEYS[2], unpack(active_rules)); " +
                    "local result = {}; " +
                    "for i, rule_id in ipairs(active_rules) do " +
                    "    result[i] = rule_id .. '|||' .. (rule_data[i] or ''); " +
                    "end; " +
                    "return result;";

            @SuppressWarnings("unchecked")
            List<String> ruleData = (List<String>) jedis.eval(luaScript, 2, ACTIVE_RULES_SET_KEY, RULES_HASH_KEY);

            Map<String, RiskRule> newRules = new HashMap<>();
            for (String ruleEntry : ruleData) {
                String[] parts = ruleEntry.split("\\|\\|\\|", 2);
                if (parts.length == 2) {
                    String ruleId = parts[0];
                    String ruleJson = parts[1];
                    try {
                        RiskRule rule = JSON.parseObject(ruleJson, RiskRule.class);
                        if (rule != null) {
                            newRules.put(ruleId, rule);
                        }
                    } catch (Exception e) {
                        logger.error("解析规则失败，规则ID: {}", ruleId, e);
                    }
                }
            }
            rulesMapRef.set(Collections.unmodifiableMap(newRules));
            logger.info("规则加载成功，规则数量: {}", newRules.size());
        } catch (Exception e) {
            logger.error("加载规则失败", e);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
        onlineModel.shutdown();
    }

    public static class RuleResult {
        private boolean triggered;
        private String action;
        private String ruleId;
        private Object evidence;

        public boolean isTriggered() { return triggered; }
        public void setTriggered(boolean triggered) { this.triggered = triggered; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public Object getEvidence() { return evidence; }
        public void setEvidence(Object evidence) { this.evidence = evidence; }
    }
}