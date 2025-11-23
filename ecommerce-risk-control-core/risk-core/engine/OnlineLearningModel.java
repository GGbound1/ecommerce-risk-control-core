package com.risk.core.engine;

import com.risk.core.model.OrderEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 在线学习风险评分模型
 * 基于Redis实现实时风险评分更新
 */
public class OnlineLearningModel {
    private static final Logger logger = Logger.getLogger(OnlineLearningModel.class.getName());
    private final JedisPool jedisPool;
    private final ExecutorService asyncExecutor;
    private final String updateScriptSha;

    public OnlineLearningModel(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.asyncExecutor = Executors.newFixedThreadPool(16);

        // 预加载Lua脚本保证原子性
        String updateScript = "local current = redis.call('incrByFloat', KEYS[1], ARGV[1]); " +
                "if current == tonumber(ARGV[1]) then " +
                "   redis.call('expire', KEYS[1], ARGV[2]); " +
                "end " +
                "return current;";

        try (Jedis jedis = jedisPool.getResource()) {
            this.updateScriptSha = jedis.scriptLoad(updateScript);
        } catch (Exception e) {
            logger.severe("Failed to load Lua script: " + e.getMessage());
            throw new RuntimeException("Failed to load Lua script", e);
        }
    }

    public void collect(OrderEvent order, RuleEngine.RuleResult result) {
        asyncExecutor.execute(() -> updateRiskScore(order, result));
    }

    private void updateRiskScore(OrderEvent order, RuleEngine.RuleResult result) {
        if (order.getUserId() == null || order.getDeviceId() == null) {
            return;
        }
        
        String key = "risk:scores:" + order.getUserId() + ":" + order.getDeviceId();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.evalsha(updateScriptSha, 1, key, "0.1", String.valueOf(7 * 24 * 3600));
        } catch (Exception e) {
            logger.severe("风险分更新失败: " + e.getMessage());
        }
    }

    public void shutdown() {
        asyncExecutor.shutdown();
    }
}