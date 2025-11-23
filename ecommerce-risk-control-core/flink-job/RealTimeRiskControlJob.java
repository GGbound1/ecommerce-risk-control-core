package com.risk.core.flink;

import com.risk.core.engine.RuleEngine;
import com.risk.core.engine.OnlineLearningModel;
import com.risk.core.model.OrderEvent;
import com.risk.core.model.RiskEvent;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.alibaba.fastjson.JSON;

import java.util.concurrent.CompletableFuture;

/**
 * Flink实时风险控制任务
 * 处理订单事件流并执行风险评估
 */
public class RealTimeRiskControlJob extends RichFlatMapFunction<String, Tuple2<OrderEvent, String>> {

    private transient JedisPool jedisPool;
    private transient RuleEngine ruleEngine;

    @Override
    public void open(Configuration parameters) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(20);
        
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);
        OnlineLearningModel onlineModel = new OnlineLearningModel(jedisPool);
        this.ruleEngine = new RuleEngine(jedisPool, onlineModel);
    }

    @Override
    public void flatMap(String value, Collector<Tuple2<OrderEvent, String>> out) throws Exception {
        try {
            // 解析订单事件
            OrderEvent order = JSON.parseObject(value, OrderEvent.class);
            
            // 执行风险评估
            CompletableFuture<RiskEvent> future = ruleEngine.evaluateAsync(order);
            RiskEvent riskEvent = future.get();
            
            // 输出风险结果
            if (riskEvent.hasRisk()) {
                for (String action : riskEvent.getActions()) {
                    out.collect(new Tuple2<>(order, "风险动作: " + action));
                }
            }
            
        } catch (Exception e) {
            System.err.println("处理订单事件异常: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        if (ruleEngine != null) {
            ruleEngine.shutdown();
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}