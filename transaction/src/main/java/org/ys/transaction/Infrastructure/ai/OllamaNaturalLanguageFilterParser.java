package org.ys.transaction.Infrastructure.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ys.transaction.domain.port.NaturalLanguageFilterParserPort;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OllamaNaturalLanguageFilterParser implements NaturalLanguageFilterParserPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaNaturalLanguageFilterParser.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ollama.enabled:true}")
    private boolean enabled;

    @Value("${app.ollama.parse-timeout-ms:12000}")
    private long parseTimeoutMs;

    /** 为空则沿用 spring.ai.ollama.chat.options.model；建议单独拉取小模型如 qwen2.5:3b 专用于解析 */
    @Value("${app.ollama.parser-model:}")
    private String parserModel;

    @Value("${app.ollama.parser-num-predict:256}")
    private int parserNumPredict;

    @Value("${app.ollama.parser-num-ctx:2048}")
    private int parserNumCtx;

    @Value("${app.ollama.parser-temperature:0.1}")
    private double parserTemperature;

    @Value("${app.ollama.parser-json-format:true}")
    private boolean parserJsonFormat;

    public OllamaNaturalLanguageFilterParser(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> parseNaturalLanguage(String nlQuery) {
        Map<String, Object> result = new HashMap<>();
        if (!enabled || nlQuery == null || nlQuery.trim().isEmpty()) {
            return result;
        }
        try {
            String prompt = buildPrompt(nlQuery.trim());
            OllamaOptions options = buildParserOptions();
            log.debug("[NL解析] 调用 Ollama, timeoutMs={}, options.model={}", parseTimeoutMs,
                    options.getModel() != null ? options.getModel() : "(default)");
            String response;
            try {
                response = CompletableFuture
                        .supplyAsync(() -> chatClient.prompt().user(prompt).options(options).call().content())
                        .get(parseTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                log.warn("Ollama parse timeout/fail, fallback to normal filters. query={}", nlQuery, ex);
                return result;
            }
            if (response == null || response.trim().isEmpty()) {
                return result;
            }
            String jsonText = cleanupResponse(response);
            Map<String, Object> parsed = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
            sanitize(parsed);
            return parsed;
        } catch (Exception ignore) {
            return result;
        }
    }

    /**
     * OllamaOptions.Builder b = OllamaOptions.builder()
     *     .numPredict(256)        // ✅ JSON 很短，256 tokens 足够
     *     .numCtx(2048)           // ✅ 简单查询不需要大上下文
     *     .temperature(0.1)       // ✅ 需要确定性输出，不要创造性
     *     .topP(0.9);             // ✅ 平衡准确性和灵活性
     * @return
     */
    private OllamaOptions buildParserOptions() {
        OllamaOptions.Builder b = OllamaOptions.builder()
                .numPredict(parserNumPredict)
                .numCtx(parserNumCtx)
                .temperature(parserTemperature)
                .topP(0.9);
        if (parserModel != null && !parserModel.trim().isEmpty()) {
            b.model(parserModel.trim());
        }
        if (parserJsonFormat) {
            b.format("json");
        }
        return b.build();
    }

    private static String buildPrompt(String query) {
        return "你是一个电商查询解析器。仅将自然语言转换为 JSON 格式的过滤器。\n"
                + "只返回 JSON，不要包含 Markdown 格式，不要任何解释。\n"
                + "允许的键名：keyword（关键词）, brand（品牌）, category（分类名称）, categoryId（分类ID）, priceMin（最低价格）, priceMax（最高价格）, inventoryMin（最小库存）, sort（排序）。\n"
                + "sort 的值必须是以下之一：newest（最新）, priceAsc（价格升序）, priceDesc（价格降序）, inventoryAsc（库存升序）, inventoryDesc（库存降序）。\n"
                + "不要输出未定义的键名。\n"
                + "用户输入：" + query;
    }

    private static String cleanupResponse(String response) {
        String text = response.trim();
        if (text.startsWith("```")) {
            int firstBreak = text.indexOf('\n');
            if (firstBreak > 0) {
                text = text.substring(firstBreak + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
        }
        return text.trim();
    }

    private static void sanitize(Map<String, Object> map) {
        if (map == null) return;
        map.entrySet().removeIf(e -> e.getValue() == null || e.getValue().toString().trim().isEmpty());
        if (map.containsKey("sort")) {
            String sort = String.valueOf(map.get("sort"));
            if (!"newest".equals(sort)
                    && !"priceAsc".equals(sort)
                    && !"priceDesc".equals(sort)
                    && !"inventoryAsc".equals(sort)
                    && !"inventoryDesc".equals(sort)) {
                map.remove("sort");
            }
        }
    }
}
