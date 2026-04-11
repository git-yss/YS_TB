package org.ys.transaction.Infrastructure.rag;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j：向量化、切块、{@link EmbeddingStore}、{@link EmbeddingStoreContentRetriever}。
 * 当前默认进程内 {@link InMemoryEmbeddingStore}；多实例部署时各节点索引不一致，生产请换共享向量库。
 */
@Configuration
@EnableConfigurationProperties(RagProperties.class)
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true", matchIfMissing = true)
public class LangChain4jRagConfiguration {

    public static final String RAG_EMBEDDING_MODEL = "ragEmbeddingModel";
    public static final String RAG_EMBEDDING_STORE = "ragEmbeddingStore";
    public static final String RAG_INGESTOR = "ragEmbeddingStoreIngestor";

    @Bean(name = RAG_EMBEDDING_MODEL)
    public EmbeddingModel ragEmbeddingModel(RagProperties props) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(props.getOllamaBaseUrl())
                .modelName(props.getEmbeddingModel())
                .timeout(Duration.ofSeconds(props.getHttpTimeoutSeconds()))
                .build();
    }

    @Bean(name = RAG_EMBEDDING_STORE)
    public EmbeddingStore<TextSegment> ragEmbeddingStore(RagProperties props) {
        if (!"memory".equalsIgnoreCase(props.getStoreType())) {
            throw new IllegalStateException(
                    "app.rag.storeType=" + props.getStoreType() + " 尚未接入，请使用 memory 或扩展本配置类注册 Redis/PG 等 EmbeddingStore Bean。");
        }
        return new InMemoryEmbeddingStore<>();
    }

    @Bean(name = RAG_INGESTOR)
    public EmbeddingStoreIngestor ragEmbeddingStoreIngestor(
            RagProperties props,
            EmbeddingModel ragEmbeddingModel,
            EmbeddingStore<TextSegment> ragEmbeddingStore) {
        return EmbeddingStoreIngestor.builder()
                .embeddingModel(ragEmbeddingModel)
                .embeddingStore(ragEmbeddingStore)
                .documentSplitter(DocumentSplitters.recursive(
                        Math.max(200, props.getChunkMaxChars()),
                        Math.max(0, Math.min(props.getChunkOverlapChars(), props.getChunkMaxChars() - 1))))
                .build();
    }
}
