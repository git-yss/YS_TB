package org.ys.transaction.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AssistantApplicationService {
    private static final Logger log = LoggerFactory.getLogger(AssistantApplicationService.class);
    private static final int MAX_MSG_LEN = 300;
    private static final int MAX_HISTORY = 10;
    private static final ConcurrentHashMap<String, List<String>> SESSION_HISTORY = new ConcurrentHashMap<>();

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Resource
    private AssistantToolService assistantToolService;

    @Resource
    private ObjectMapper objectMapper;

    public Map<String, Object> chat(Long userId, String message, String sessionId) {
        long start = System.currentTimeMillis();
        String sid = normalizeSessionId(sessionId);
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
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = buildUserPrompt(userId, sid, content);
            String ai = chatClient.prompt()
                    .system(buildSystemPrompt())
                    .user(prompt)
                    .tools(assistantToolService)
                    .call()
                    .content();
            Map<String, Object> result = parseAiResponse(ai);
            result.put("sessionId", sid);
            appendHistory(sid, "U:" + content);
            appendHistory(sid, "A:" + String.valueOf(result.get("reply")));
            log.info("assistant_chat_ok userId={} sessionId={} costMs={} q={}", userId, sid, (System.currentTimeMillis() - start), content);
            return result;
        } catch (Exception e) {
            log.warn("assistant_chat_fail userId={} sessionId={} costMs={} q={}", userId, sid, (System.currentTimeMillis() - start), content, e);
            return replyWithSession("我刚刚没有理解成功，请换个说法再试一次。", sid);
        }
    }

    private static Map<String, Object> reply(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("reply", text);
        return map;
    }

    private static String buildSystemPrompt() {
        return "你是电商智能客服助手。你必须优先通过工具查询真实数据，不要编造订单、支付和商品信息。"
                + "当用户要查订单或支付时，必须使用工具。"
                + "若用户未登录，不要捏造订单数据，应提示先登录。"
                + "最终只返回 JSON，格式: {\"reply\":\"...\",\"type\":\"text|order_list|payment_status|goods_search\",\"items\":[],\"result\":{},\"order\":{}}。";
    }

    private static String buildUserPrompt(Long userId, String sessionId, String content) {
        List<String> history = SESSION_HISTORY.getOrDefault(sessionId, new ArrayList<String>());
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

    private static String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionId.trim();
    }

    private static boolean containsForbiddenAction(String content) {
        String c = content.toLowerCase();
        return c.contains("删库") || c.contains("删除用户") || c.contains("管理员密码");
    }

    private static void appendHistory(String sessionId, String line) {
        SESSION_HISTORY.compute(sessionId, (k, oldList) -> {
            List<String> list = oldList == null ? new ArrayList<String>() : new ArrayList<String>(oldList);
            list.add(line);
            while (list.size() > MAX_HISTORY) {
                list.remove(0);
            }
            return list;
        });
    }

    private static Map<String, Object> replyWithSession(String text, String sessionId) {
        Map<String, Object> map = reply(text);
        map.put("sessionId", sessionId);
        return map;
    }
}
