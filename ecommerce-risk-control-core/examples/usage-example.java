import com.risk.core.engine.RuleEngine;
import com.risk.core.engine.OnlineLearningModel;
import com.risk.core.model.OrderEvent;
import com.risk.core.model.RiskEvent;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 规则引擎使用示例
 * 演示如何初始化规则引擎并执行风险评估
 */
public class RuleEngineExample {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== 风控规则引擎使用示例 ===");
        
        // 1. 初始化Redis连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379);
        
        // 2. 初始化在线学习模型和规则引擎
        OnlineLearningModel onlineModel = new OnlineLearningModel(jedisPool);
        RuleEngine ruleEngine = new RuleEngine(jedisPool, onlineModel);
        
        // 3. 创建测试订单事件
        OrderEvent order = new OrderEvent();
        order.setOrderId("test_order_001");
        order.setUserId("user_123");
        order.setAmount(6000.0);
        order.setDeviceId("device_456");
        order.setIp("192.168.1.100");
        order.setTimestamp(System.currentTimeMillis());
        
        // 设置商品信息
        order.setItems(new HashMap<>());
        order.getItems().put("product_001", "2");
        order.getItems().put("product_002", "1");
        
        System.out.println("创建测试订单: " + order.getOrderId());
        System.out.println("订单金额: " + order.getAmount());
        
        // 4. 执行风险评估
        CompletableFuture<RiskEvent> future = ruleEngine.evaluateAsync(order);
        RiskEvent riskEvent = future.get(5, TimeUnit.SECONDS);
        
        // 5. 处理评估结果
        System.out.println("\n=== 风险评估结果 ===");
        if (riskEvent.hasRisk()) {
            System.out.println("发现风险事件!");
            System.out.println("风险动作: " + riskEvent.getActions());
            System.out.println("风险上下文: " + riskEvent.getContext());
        } else {
            System.out.println("无风险事件");
        }
        
        // 6. 清理资源
        ruleEngine.shutdown();
        jedisPool.close();
        
        System.out.println("\n示例执行完成!");
    }
}