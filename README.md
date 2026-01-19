# 电商实时风控系统核心组件
[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green)](https://spring.io/)
[![Vue 3](https://img.shields.io/badge/Vue%203-Composition%20API-brightgreen)](https://vuejs.org/)
[![Apache Flink](https://img.shields.io/badge/Apache%20Flink-1.16-orange)](https://flink.apache.org/)

一个高性能的电商实时风控系统核心组件库，提供规则引擎、机器学习风险评估、实时风险处理和可视化监控能力。

## 核心特性
- **高性能规则引擎** - 支持并行规则评估，QPS可达10k+，支持Redis原子操作和规则热更新
- **实时风险监控** - 基于Apache Flink的流式处理，毫秒级响应，支持Stream数据结构和事件时间处理
- **机器学习集成** - 集成线性回归模型进行智能风险评估，支持多维度特征提取和个性化评估
- **多维度可视化** - 丰富的风险数据分析和监控看板，支持用户风险画像和行为模式分析
- **模块化架构** - 高度可扩展的插件化设计，支持SPI规则加载和协调器机制
- **在线学习** - 实时风险评分更新，基于Redis的渐进式风险学习

## 系统架构
```
订单数据 → Kafka → Flink实时处理 →
├─ 规则引擎评估（基础规则 + 机器学习） → 风险评估结果
├─ 图分析引擎 → 关联关系检测
├─ 在线学习模型 → 风险评分更新
└─ Redis Stream持久化 → Spring Boot API → 监控系统
```

## 快速开始
### 环境要求
- Java 17+
- Apache Flink 1.16+
- Redis 7.0+（支持Stream数据结构）
- Spring Boot 3.0+
- MySQL 8.0+

### 后端核心使用
```java
// 初始化风控系统
JedisPool jedisPool = new JedisPool("localhost", 6379);
OnlineLearningModel onlineModel = new OnlineLearningModel(jedisPool);
MLRiskPredictor mlPredictor = new MLRiskPredictor(jedisPool);
RuleEngine ruleEngine = new RuleEngine(jedisPool, onlineModel);

// 创建订单事件
OrderEvent order = new OrderEvent();
order.setOrderId("order_123");
order.setUserId("user_456"); 
order.setAmount(1500.0);
order.setDeviceId("device_789");
order.setIp("192.168.1.100");

// 执行风险评估
CompletableFuture<RiskEvent> result = ruleEngine.evaluateAsync(order);
RiskEvent riskEvent = result.get(5, TimeUnit.SECONDS);

// 获取用户风险画像
Map<String, Object> profile = riskController.getUserRiskProfile("user_456");
```

### 前端组件使用
```vue
<template>
  <RiskDashboard>
    <RiskChart type="line" :data="trendData" title="风险事件趋势"/>
    <UserProfilePanel :userId="currentUserId"/>
    <StrategyConfigPanel @update="handleStrategyUpdate"/>
  </RiskDashboard>
</template>

<script setup>
import { RiskChart, UserProfilePanel, StrategyConfigPanel } from './components';
</script>
```

## 核心模块
### 1. 规则引擎 (RuleEngine)
- 异步并行规则评估：支持`CompletableFuture`异步执行
- 动态规则热加载：基于Redis的规则存储和实时更新
- 高性能规则匹配：使用不可变规则快照保证一致性
- 机器学习集成：规则评估与机器学习评分结合
- 规则变更监听：Redis Pub/Sub实时通知机制

### 2. 机器学习风险评估器 (MLRiskPredictor)
- 多维度特征提取：金额、频率、设备、IP、时间五维度评分
- 线性回归模型：基于Apache Commons Math实现
- 用户个性化偏差：基于用户历史行为的风险调整
- 模型持久化：Redis存储和热加载支持
- 实时预测：毫秒级风险评估响应

### 3. 在线学习模型 (OnlineLearningModel)
- 实时风险评分更新：基于订单事件和规则结果
- 原子性操作：Lua脚本保证Redis操作原子性
- 异步更新机制：线程池处理不影响主流程
- 渐进式学习：持续优化风险评分模型

### 4. Flink实时处理
- 流式处理流水线：Kafka消费 → 规则评估 → Redis存储
- 状态管理：基于Flink状态API的规则统计
- 错误处理：死信队列和异常恢复机制
- 性能优化：并行处理和异步I/O

### 5. Spring Boot后端服务
- RESTful API：完整的风控管理接口
- 配置管理：风险策略、API配置集中管理
- 用户认证：JWT token认证机制
- 异常处理：统一异常处理和结果封装
- 数据脱敏：用户信息脱敏处理

### 6. 规则协调器 (RuleCoordinator)
- 并行执行优化：多规则并行评估
- 插件化架构：SPI服务加载机制
- 运行时管理：规则执行上下文协调
- 错误隔离：单规则失败不影响其他规则

## 系统功能
### 风险控制API
| 接口功能 | 请求方式 | 接口地址 | 说明 |
|---------|---------|---------|------|
| 风险评估 | POST | `/api/risk/assess` | 订单风险评估 |
| 策略管理/查询 | GET/POST | `/api/risk/strategy` | 策略管理和查询 |
| 用户风险画像 | GET | `/api/risk/profile/{userId}` | 用户风险画像 |
| 规则CRUD | GET/POST/PUT/DELETE | `/api/rules` | 规则增删改查 |
| 风险事件分页查询 | GET | `/api/events` | 风险事件分页查询 |

### 数据统计API
| 接口功能 | 请求方式 | 接口地址 | 说明 |
|---------|---------|---------|------|
| 风险数据统计 | GET | `/api/stats` | 数据看板核心数据 |
| 风险趋势分析 | GET | `/api/trend` | 风险趋势分析 |
| 事件类型分布 | GET | `/api/distribution` | 事件类型分布 |
| 最近风险事件 | GET | `/api/recent` | 实时监控最近风险事件 |

### 系统管理API
| 接口功能 | 请求方式 | 接口地址 | 说明 |
|---------|---------|---------|------|
| 用户登录 | POST | `/api/auth/login` | 用户登录获取JWT令牌 |
| 用户注册 | POST | `/api/auth/register` | 用户注册 |
| 异常记录管理 | GET/POST | `/api/exception/*` | 异常记录查询与处理 |
| API配置管理 | GET/POST | `/api/system/api-config` | 系统配置管理 |

## 技术实现
### 数据处理流程
1.  数据接入：Kafka接收订单事件流
2.  实时处理：Flink作业进行流式处理
3.  规则评估：并行执行基础规则和机器学习评估
4.  结果存储：Redis Stream存储风险事件
5.  API服务：Spring Boot提供数据查询和管理接口

### 存储设计
#### Redis数据结构
| 键名 | 数据类型 | 用途 |
|------|---------|------|
| `risk:rules:hash` | Hash | 规则定义存储 |
| `risk:rules:active` | Set | 活跃规则集合 |
| `risk:event:stream` | Stream | 风险事件Stream |
| `risk:structured_events` | List | 风险事件List（兼容） |
| `ml:model:{userId}` | Hash | 用户机器学习模型 |

#### MySQL存储
- 用户信息、异常记录等持久化业务数据
- 系统配置、操作日志等非实时性数据

### 性能优化
- 缓存策略：Redis多级缓存，热点数据内存化
- 异步处理：`CompletableFuture`异步执行，线程池优化
- 连接池管理：Jedis连接池、数据库连接池优化
- 序列化优化：FastJSON序列化，减少网络传输
- 批量操作：Redis管道和批量操作减少RTT

## 配置指南
### 应用配置（application.yml）
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/risk_control
    username: root
    password: password
  
  redis:
    host: 192.168.xxx.xxx
    port: 6379
    password: password
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000

risk:
  strategy:
    mlEnabled: true
    thresholds:
      block: 80.0
      review: 70.0
      verify: 50.0
      warning: 40.0
    featureWeights:
      amount: 0.3
      frequency: 0.25
      device: 0.2
      ip: 0.15
      time: 0.1
    autoLearn:
      enabled: true
      sampleSize: 1000
      retrainInterval: 3600
```

### Flink作业配置
```java
// Flink作业启动参数
env.setParallelism(8);
env.enableCheckpointing(60000);
env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
```

## 部署方案
### 开发环境
```bash
# 启动Kafka
bin/kafka-server-start.sh config/server.properties

# 启动Flink作业
./bin/flink run -c com.ec.risk.model.RealTimeRiskControlJob risk-control-job.jar

# 启动Redis
redis-server --requirepass 123456

# 启动Spring Boot应用
mvn spring-boot:run
```

### 生产环境建议
#### 高可用架构
- Redis集群（哨兵模式）
- Kafka集群（多副本）
- Flink集群（高可用配置）
- 负载均衡（Nginx/Haproxy）

#### 监控告警
- Prometheus + Grafana监控
- ELK日志收集
- 告警规则配置

#### 性能优化
- JVM参数调优
- 连接池配置优化
- 缓存策略优化

## 项目结构说明
```
com.ec.risk
├── common        # 通用工具类和结果封装
├── config        # 系统配置类
├── controller    # Spring MVC控制器
├── engine        # 核心引擎（规则引擎、机器学习等）
├── entity        # 实体类定义
├── mapper        # MyBatis映射器
├── model         # 数据模型和Flink作业
├── service       # 业务服务层
└── utils         # 工具类
```
##部分功能展示：

<img width="415" height="213" alt="image" src="https://github.com/user-attachments/assets/ec248874-fdbe-4776-8bc0-da8c737ec79f" />

<img width="415" height="211" alt="image" src="https://github.com/user-attachments/assets/fbd8e7ae-7731-4a75-8284-15a62a1f70f8" />

<img width="415" height="248" alt="image" src="https://github.com/user-attachments/assets/8c1152e5-e720-4e3a-8527-e6914b5d576d" />

<img width="416" height="249" alt="image" src="https://github.com/user-attachments/assets/5210d24c-400d-460a-ba8e-bed40c44aadf" />


## 许可证
MIT License - 查看 LICENSE 文件了解详情。
