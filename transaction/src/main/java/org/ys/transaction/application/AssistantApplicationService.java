package org.ys.transaction.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.Resource;
import org.ys.transaction.Infrastructure.ai.AssistantToolService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class AssistantApplicationService {
    private static final Logger log = LoggerFactory.getLogger(AssistantApplicationService.class);
    private static final int MAX_MSG_LEN = 300;

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Resource
    private AssistantToolService assistantToolService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedissonClient redissonClient;

    @Value("${app.assistant.memory-ttl-hours:24}")
    private long memoryTtlHours;

    @Value("${app.assistant.max-history-lines:100}")
    private int maxHistoryLines;

    @Value("${app.assistant.max-history-prompt-chars:12000}")
    private int maxHistoryPromptChars;

    @Value("${app.assistant.max-reply-chars-in-history:800}")
    private int maxReplyCharsInHistory;

    public Map<String, Object> chat(Long userId, String message, String sessionId) {
        long start = System.currentTimeMillis();
        String userKey = buildUserKey(userId);
        String sid = normalizeSessionId(sessionId, userKey);
        String content = message == null ? "" : message.trim();
        if (content.length() > MAX_MSG_LEN) {
            return replyWithSession("问题太长了，请控制在 300 字以内。", sid);
        }
        if (containsForbiddenAction(content)) {
            return replyWithSession("抱歉，这类请求我不能处理。我可以帮你查询订单、支付状态、商品信息。", sid);
        }
        if (content.isEmpty()) {
            return replyWithSession("你好，我可以帮你查订单、支付状态和商品信息。", sid);
        }
        try {
            assistantToolService.resetTrace();
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = buildUserPrompt(userId, userKey, sid, content);
            String ai = chatClient.prompt()
                    .system(Objects.requireNonNull(buildSystemPrompt()))
                    .user(Objects.requireNonNull(prompt))
                    .tools(assistantToolService)
                    .call()
                    .content();
            Map<String, Object> result = parseAiResponse(ai);
            result.put("sessionId", sid);
            appendHistory(userKey, sid, "U:" + content);
            appendHistory(userKey, sid, "A:" + truncateForHistory(String.valueOf(result.get("reply"))));
            touchSession(userKey, sid, content);
            result.put("toolTraces", assistantToolService.getAndClearTrace());
            log.info("assistant_chat_ok userId={} sessionId={} costMs={} q={}", userId, sid, (System.currentTimeMillis() - start), content);
            return result;
        } catch (Exception e) {
            List<Map<String, Object>> traces = assistantToolService.getAndClearTrace();
            log.warn("assistant_chat_fail userId={} sessionId={} costMs={} q={}", userId, sid, (System.currentTimeMillis() - start), content, e);
            Map<String, Object> fail = replyWithSession("我刚刚没有理解成功，请换个说法再试一次。", sid);
            fail.put("toolTraces", traces);
            return fail;
        }
    }

    /**
     * 流式对话：{@code onDelta} 输出模型 token；结束时 {@code onFinal} 恰好调用一次（成功或失败），结构与 {@link #chat} 返回一致。
     */
    public void streamChat(Long userId, String message, String sessionId,
                           Consumer<String> onDelta,
                           Consumer<Map<String, Object>> onFinal) {
        long start = System.currentTimeMillis();
        String userKey = buildUserKey(userId);
        String sid = normalizeSessionId(sessionId, userKey);
        String content = message == null ? "" : message.trim();
        if (content.length() > MAX_MSG_LEN) {
            onFinal.accept(replyWithSession("问题太长了，请控制在 300 字以内。", sid));
            return;
        }
        if (containsForbiddenAction(content)) {
            onFinal.accept(replyWithSession("抱歉，这类请求我不能处理。我可以帮你查询订单、支付状态、商品信息。", sid));
            return;
        }
        if (content.isEmpty()) {
            onFinal.accept(replyWithSession("你好，我可以帮你查订单、支付状态和商品信息。", sid));
            return;
        }
        try {
            assistantToolService.resetTrace();
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = buildUserPrompt(userId, userKey, sid, content);
            StringBuilder acc = new StringBuilder();
            Flux<String> flux = chatClient.prompt()
                    .system(Objects.requireNonNull(buildSystemPrompt()))
                    .user(Objects.requireNonNull(prompt))
                    .tools(assistantToolService)
                    .stream()
                    .content()
                    .subscribeOn(Schedulers.boundedElastic());
            flux.subscribe(
                    token -> {
                        if (token != null) {
                            acc.append(token);
                            onDelta.accept(token);
                        }
                    },
                    err -> {
                        List<Map<String, Object>> traces = assistantToolService.getAndClearTrace();
                        log.warn("assistant_chat_stream_fail userId={} sessionId={} costMs={} q={}",
                                userId, sid, (System.currentTimeMillis() - start), content, err);
                        Map<String, Object> fail = replyWithSession("我刚刚没有理解成功，请换个说法再试一次。", sid);
                        fail.put("toolTraces", traces);
                        onFinal.accept(fail);
                    },
                    () -> {
                        try {
                            Map<String, Object> result = parseAiResponse(acc.toString());
                            result.put("sessionId", sid);
                            appendHistory(userKey, sid, "U:" + content);
                            appendHistory(userKey, sid, "A:" + truncateForHistory(String.valueOf(result.get("reply"))));
                            touchSession(userKey, sid, content);
                            result.put("toolTraces", assistantToolService.getAndClearTrace());
                            log.info("assistant_chat_stream_ok userId={} sessionId={} costMs={} q={}",
                                    userId, sid, (System.currentTimeMillis() - start), content);
                            onFinal.accept(result);
                        } catch (Exception e) {
                            List<Map<String, Object>> traces = assistantToolService.getAndClearTrace();
                            log.warn("assistant_chat_stream_parse_fail userId={} sessionId={}", userId, sid, e);
                            Map<String, Object> fail = replyWithSession("我刚刚没有理解成功，请换个说法再试一次。", sid);
                            fail.put("toolTraces", traces);
                            onFinal.accept(fail);
                        }
                    });
        } catch (Exception e) {
            List<Map<String, Object>> traces = assistantToolService.getAndClearTrace();
            log.warn("assistant_chat_stream_setup_fail userId={} sessionId={}", userId, sid, e);
            Map<String, Object> fail = replyWithSession("我刚刚没有理解成功，请换个说法再试一次。", sid);
            fail.put("toolTraces", traces);
            onFinal.accept(fail);
        }
    }

    public Map<String, Object> createSession(Long userId, String title) {
        String userKey = buildUserKey(userId);
        String sid = UUID.randomUUID().toString();
        RMap<String, Map<String, Object>> sessions = sessionMap(userKey);
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("sessionId", sid);
        meta.put("title", (title == null || title.trim().isEmpty()) ? "新会话" : title.trim());
        meta.put("updatedAt", System.currentTimeMillis());
        meta.put("preview", "");
        sessions.put(sid, meta);
        sessions.expire(java.time.Duration.ofHours(memoryTtlHours));
        clearHistory(userKey, sid);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("sessionId", sid);
        result.put("title", meta.get("title"));
        return result;
    }

    public List<Map<String, Object>> listSessions(Long userId) {
        String userKey = buildUserKey(userId);
        RMap<String, Map<String, Object>> sessions = sessionMap(userKey);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Map<String, Object>> e : sessions.entrySet()) {
            Map<String, Object> row = new HashMap<String, Object>(e.getValue());
            row.put("sessionId", e.getKey());
            list.add(row);
        }
        list.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                long x = toLong(a.get("updatedAt"));
                long y = toLong(b.get("updatedAt"));
                return Long.compare(y, x);
            }
        });
        sessions.expire(java.time.Duration.ofHours(memoryTtlHours));
        return list;
    }

    public boolean deleteSession(Long userId, String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return false;
        String userKey = buildUserKey(userId);
        String sid = sessionId.trim();
        RMap<String, Map<String, Object>> sessions = sessionMap(userKey);
        boolean removed = sessions.remove(sid) != null;
        clearHistory(userKey, sid);
        sessions.expire(java.time.Duration.ofHours(memoryTtlHours));
        return removed;
    }

    public List<Map<String, Object>> history(Long userId, String sessionId) {
        String sid = sessionId == null ? "" : sessionId.trim();
        if (sid.isEmpty()) return new ArrayList<Map<String, Object>>();
        String userKey = buildUserKey(userId);
        List<String> history = loadHistory(userKey, sid);
        List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
        for (String line : history) {
            if (line == null) continue;
            Map<String, Object> msg = new HashMap<String, Object>();
            if (line.startsWith("U:")) {
                msg.put("role", "user");
                msg.put("text", line.substring(2));
            } else if (line.startsWith("A:")) {
                msg.put("role", "bot");
                msg.put("text", line.substring(2));
            } else {
                msg.put("role", "bot");
                msg.put("text", line);
            }
            messages.add(msg);
        }
        return messages;
    }

    private static Map<String, Object> reply(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("reply", text);
        return map;
    }

    private static String buildSystemPrompt() {
        return "你是电商智能客服助手。你必须优先通过工具查询真实数据，不要编造订单、支付和商品信息。"
                + "当用户要查订单或支付时，必须使用工具。"
                + "当用户询问退换货规则、活动政策、平台条款等知识型问题时，应先使用知识库检索工具获取依据再回答。"
                + "若用户未登录，不要捏造订单数据，应提示先登录。"
                + "最终只返回 JSON，格式: {\"reply\":\"...\",\"type\":\"text|order_list|payment_status|goods_search\",\"items\":[],\"result\":{},\"order\":{}}。";
    }

    private String buildUserPrompt(Long userId, String userKey, String sessionId, String content) {
        List<String> history = trimHistoryForPrompt(loadHistory(userKey, sessionId));
        StringBuilder sb = new StringBuilder();
        sb.append("当前登录用户ID: ").append(userId == null ? "null" : userId).append("\n");
        sb.append("会话ID: ").append(sessionId).append("\n");
        if (!history.isEmpty()) {
            sb.append("最近对话历史:\n");
            for (String h : history) {
                sb.append(h).append("\n");
            }
        }
        sb.append("用户问题: ").append(content);
        return sb.toString();
    }

    /**
     * 从最近一轮往前取历史，总字符不超过 maxHistoryPromptChars，避免上下文过长拖慢推理。
     */
    private List<String> trimHistoryForPrompt(List<String> chronological) {
        if (chronological == null || chronological.isEmpty()) {
            return chronological;
        }
        int budget = Math.max(0, maxHistoryPromptChars);
        List<String> picked = new ArrayList<>();
        int used = 0;
        for (int i = chronological.size() - 1; i >= 0; i--) {
            String line = chronological.get(i);
            if (line == null) {
                continue;
            }
            String clipped = line.length() > budget ? line.substring(0, budget) + "…" : line;
            int addLen = clipped.length() + 1;
            if (!picked.isEmpty() && used + addLen > budget) {
                break;
            }
            picked.add(clipped);
            used += addLen;
        }
        Collections.reverse(picked);
        return picked;
    }

    private String truncateForHistory(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxReplyCharsInHistory) {
            return text;
        }
        return text.substring(0, maxReplyCharsInHistory) + "…";
    }

    private Map<String, Object> parseAiResponse(String ai) {
        if (ai == null || ai.trim().isEmpty()) {
            return reply("我暂时没有拿到结果，请稍后再试。");
        }
        try {
            String text = ai.trim();
            if (text.startsWith("```")) {
                int idx = text.indexOf('\n');
                if (idx > 0) text = text.substring(idx + 1);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
            }
            return objectMapper.readValue(text.trim(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignore) {
            return reply(ai);
        }
    }

    private String normalizeSessionId(String sessionId, String userKey) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            Map<String, Object> created = createSession("guest".equals(userKey) ? null : Long.valueOf(userKey), "默认会话");
            return String.valueOf(created.get("sessionId"));
        }
        return sessionId.trim();
    }

    private static boolean containsForbiddenAction(String content) {
        String c = content.toLowerCase();
        return c.contains("删库") || c.contains("删除用户") || c.contains("管理员密码");
    }

    private void appendHistory(String userKey, String sessionId, String line) {
        String key = historyKey(userKey, sessionId);
        RList<String> list = redissonClient.getList(key);
        list.add(line);
        int cap = Math.max(1, maxHistoryLines);
        while (list.size() > cap) {
            list.remove(0);
        }
        list.expire(java.time.Duration.ofHours(memoryTtlHours));
    }

    private static Map<String, Object> replyWithSession(String text, String sessionId) {
        Map<String, Object> map = reply(text);
        map.put("sessionId", sessionId);
        return map;
    }

    private String buildUserKey(Long userId) {
        return userId == null ? "guest" : String.valueOf(userId);
    }

    private String historyKey(String userKey, String sessionId) {
        return "assistant:user:" + userKey + ":session:" + sessionId + ":history";
    }

    private List<String> loadHistory(String userKey, String sessionId) {
        String key = historyKey(userKey, sessionId);
        RList<String> list = redissonClient.getList(key);
        List<String> history = new ArrayList<String>(list.readAll());
        if (!history.isEmpty()) {
            list.expire(java.time.Duration.ofHours(memoryTtlHours));
        }
        return history;
    }

    private RMap<String, Map<String, Object>> sessionMap(String userKey) {
        return redissonClient.getMap("assistant:user:" + userKey + ":sessions");
    }

    private void touchSession(String userKey, String sid, String preview) {
        RMap<String, Map<String, Object>> sessions = sessionMap(userKey);
        Map<String, Object> meta = sessions.get(sid);
        if (meta == null) {
            meta = new HashMap<String, Object>();
            meta.put("sessionId", sid);
            meta.put("title", "会话 " + sid.substring(0, 6));
        }
        meta.put("updatedAt", System.currentTimeMillis());
        meta.put("preview", preview == null ? "" : preview);
        sessions.put(sid, meta);
        sessions.expire(java.time.Duration.ofHours(memoryTtlHours));
    }

    private void clearHistory(String userKey, String sid) {
        redissonClient.getKeys().delete(historyKey(userKey, sid));
    }

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (Exception ignore) {
            return 0L;
        }
    }
}
