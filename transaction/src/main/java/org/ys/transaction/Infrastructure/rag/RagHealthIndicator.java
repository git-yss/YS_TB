package org.ys.transaction.Infrastructure.rag;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.ys.transaction.application.RagKnowledgeApplicationService;

import jakarta.annotation.Resource;

/**
 * 探测 Ollama 嵌入模型是否可用（不探测向量条数，避免重查询）。
 */
@Component("rag")
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true", matchIfMissing = true)
public class RagHealthIndicator implements HealthIndicator {

    @Resource
    private RagKnowledgeApplicationService ragKnowledgeApplicationService;

    @Override
    public Health health() {
        try {
            boolean ok = ragKnowledgeApplicationService.pingEmbed();
            if (ok) {
                return Health.up().withDetail("embedding", "ok").build();
            }
            return Health.down().withDetail("embedding", "failed").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
