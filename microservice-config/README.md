# 智能电商平台微服务架构升级指南

## 架构概述

本平台已升级为基于Spring Cloud Alibaba的微服务架构，包含以下核心组件：

### 核心组件

1. **Nacos** - 服务注册发现和配置中心
2. **Spring Cloud Gateway** - API网关
3. **OpenFeign** - 服务间调用
4. **Sentinel** - 熔断降级和限流
5. **Seata** - 分布式事务管理
6. **Redisson** - 分布式Redis客户端

## 服务列表

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| gateway-service | 8088 | API网关服务 |
| user-service | 8081 | 用户服务 |
| product-service | 8082 | 商品服务 |
| order-service | 8083 | 订单服务 |
| seckill-service | 8084 | 秒杀服务 |

## 部署步骤

### 1. 安装Nacos

```bash
# 下载Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz

# 解压
tar -xzf nacos-server-2.2.3.tar.gz

# 启动Nacos（单机模式）
cd nacos/bin
./startup.sh -m standalone
```

访问：http://localhost:8848/nacos
默认账号/密码：nacos/nacos

### 2. 安装Seata

```bash
# 下载Seata
wget https://github.com/seata/seata/releases/download/v1.5.2/seata-server-1.5.2.tar.gz

# 解压
tar -xzf seata-server-1.5.2.tar.gz

# 启动Seata
cd seata/bin
./seata-server.sh -p 8091 -h 127.0.0.1
```

访问：http://localhost:7091

### 3. 安装Sentinel

```bash
# 下载Sentinel
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动Sentinel
java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.6.jar
```

访问：http://localhost:8858
默认账号/密码：sentinel/sentinel

### 4. 初始化Seata数据库

执行以下SQL创建Seata所需的表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `seata` CHARACTER SET utf8mb4;

USE `seata`;

-- 创建undo_log表
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`        VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info`  LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`     INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`    DATETIME(3)  NOT NULL COMMENT 'create datetime',
    `log_modified`   DATETIME(3)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
```

在每个业务数据库中创建undo_log表。

### 5. 启动各个微服务

```bash
# 启动用户服务
cd user-service
mvn spring-boot:run

# 启动商品服务
cd product-service
mvn spring-boot:run

# 启动订单服务
cd order-service
mvn spring-boot:run

# 启动秒杀服务
cd seckill-service
mvn spring-boot:run

# 启动网关服务
cd gateway-service
mvn spring-boot:run
```

## 服务间调用示例

### 使用OpenFeign

```java
@FeignClient(name = "user-service")
public interface UserFeignClient {
    @GetMapping("/user/{id}")
    CommentResult getUserInfo(@PathVariable("id") Long id);
}

@Service
public class OrderService {
    @Resource
    private UserFeignClient userFeignClient;
    
    public void processOrder(Long userId, Long orderId) {
        // 调用用户服务
        CommentResult userInfo = userFeignClient.getUserInfo(userId);
        // ...
    }
}
```

### 分布式事务示例

```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
public void createOrder(OrderDTO orderDTO) {
    // 扣减库存（调用商品服务）
    productService.decreaseStock(orderDTO.getGoodsId(), orderDTO.getQuantity());
    
    // 扣减余额（调用用户服务）
    userService.decreaseBalance(orderDTO.getUserId(), orderDTO.getAmount());
    
    // 创建订单
    orderService.createOrder(orderDTO);
}
```

### Sentinel熔断降级示例

```java
@RestController
public class ProductController {
    
    @GetMapping("/goods/{id}")
    @SentinelResource(value = "getGoods", 
        blockHandler = "handleBlock",
        fallback = "handleFallback")
    public CommentResult getGoods(@PathVariable Long id) {
        return productService.getGoodsById(id);
    }
    
    // 限流降级处理
    public CommentResult handleBlock(Long id, BlockException ex) {
        return CommentResult.error("请求过于频繁，请稍后重试");
    }
    
    // 异常降级处理
    public CommentResult handleFallback(Long id, Throwable ex) {
        return CommentResult.error("服务暂时不可用");
    }
}
```

## 配置说明

### Nacos配置

在Nacos控制台创建以下配置：

#### user-service.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ys_tb?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

#### product-service.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ys_tb?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### Seata配置

在Nacos创建seata配置（file.config），配置内容参考Seata官方文档。

## 监控和运维

### 服务监控
- Nacos控制台：http://localhost:8848/nacos
- Sentinel控制台：http://localhost:8858
- Seata控制台：http://localhost:7091

### 日志查看
各服务的日志输出在对应服务的logs目录下。

### 健康检查
各服务提供actuator健康检查端点：
- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health

## 注意事项

1. **分布式事务**：涉及多个服务调用的操作需要使用@GlobalTransactional注解
2. **服务降级**：在FeignClient中配置fallback处理服务不可用情况
3. **限流配置**：在Sentinel控制台配置限流规则
4. **配置管理**：生产环境建议使用命名空间隔离不同环境的配置
5. **服务注册**：确保各服务正确注册到Nacos

## 扩展建议

1. **链路追踪**：集成SkyWalking或Zipkin实现分布式链路追踪
2. **日志收集**：集成ELK（Elasticsearch + Logstash + Kibana）实现日志集中管理
3. **监控告警**：集成Prometheus + Grafana实现性能监控和告警
4. **消息队列**：升级为RocketMQ或Kafka实现更可靠的消息传递

## 故障排查

### 服务无法注册到Nacos
1. 检查Nacos服务是否正常运行
2. 检查服务配置中的nacos.server-addr是否正确
3. 查看服务日志中的错误信息

### 分布式事务回滚失败
1. 检查Seata服务是否正常运行
2. 检查各业务数据库中undo_log表是否创建成功
3. 查看Seata服务日志中的事务处理记录

### 网关路由失败
1. 检查目标服务是否成功注册到Nacos
2. 检查Gateway的路由配置是否正确
3. 查看Gateway服务的路由日志
