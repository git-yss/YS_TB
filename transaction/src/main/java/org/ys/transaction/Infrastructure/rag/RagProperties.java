package org.ys.transaction.Infrastructure.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 知识库 RAG 配置。生产环境请将 {@link #storeType} 换为 redis / pgvector 等持久化向量库（需额外依赖与运维）。
 */
@ConfigurationProperties(prefix = "app.rag")
public class RagProperties {

    private boolean enabled = true;

    /** 与 spring.ai.ollama.base-url 对齐，可单独覆盖 */
    private String ollamaBaseUrl = "http://localhost:11434";

    /** 需 ollama pull nomic-embed-text（或同维度模型） */
    private String embeddingModel = "nomic-embed-text";

    private int chunkMaxChars = 900;
    private int chunkOverlapChars = 120;

    private int retrieveMaxResults = 6;
    private double retrieveMinScore = 0.55;

    /** 单文档正文最大字符，防止 OOM */
    private int ingestMaxChars = 200_000;

    private int httpTimeoutSeconds = 60;

    private int embedTimeoutSeconds = 90;

    /** memory：进程内；生产请换持久化实现 */
    private String storeType = "memory";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public int getChunkMaxChars() {
        return chunkMaxChars;
    }

    public void setChunkMaxChars(int chunkMaxChars) {
        this.chunkMaxChars = chunkMaxChars;
    }

    public int getChunkOverlapChars() {
        return chunkOverlapChars;
    }

    public void setChunkOverlapChars(int chunkOverlapChars) {
        this.chunkOverlapChars = chunkOverlapChars;
    }

    public int getRetrieveMaxResults() {
        return retrieveMaxResults;
    }

    public void setRetrieveMaxResults(int retrieveMaxResults) {
        this.retrieveMaxResults = retrieveMaxResults;
    }

    public double getRetrieveMinScore() {
        return retrieveMinScore;
    }

    public void setRetrieveMinScore(double retrieveMinScore) {
        this.retrieveMinScore = retrieveMinScore;
    }

    public int getIngestMaxChars() {
        return ingestMaxChars;
    }

    public void setIngestMaxChars(int ingestMaxChars) {
        this.ingestMaxChars = ingestMaxChars;
    }

    public int getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }

    public void setHttpTimeoutSeconds(int httpTimeoutSeconds) {
        this.httpTimeoutSeconds = httpTimeoutSeconds;
    }

    public int getEmbedTimeoutSeconds() {
        return embedTimeoutSeconds;
    }

    public void setEmbedTimeoutSeconds(int embedTimeoutSeconds) {
        this.embedTimeoutSeconds = embedTimeoutSeconds;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }
}
