package org.ys.transaction.domain.port;

/**
 * 事件发布端口（基础设施实现：MQ/JMS/Kafka 等）
 */
public interface EventPublisherPort {
    void publish(String topicOrQueue, Object payload);
}
