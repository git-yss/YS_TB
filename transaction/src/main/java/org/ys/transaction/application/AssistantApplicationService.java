package org.ys.transaction.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.ys.transaction.Infrastructure.ai.AssistantToolService;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssistantApplicationService {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Resource
    private AssistantToolService assistantToolService;

    @Resource
    private ObjectMapper objectMapper;

    public Map<String, Object> chat(Long userId, String message) {
        String content = message == null ? "" : message.trim();
        if (content.isEmpty()) {
            return reply("你好，我可以帮你查订单、支付状态和商品信息。");
        }
        try {
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = buildUserPrompt(userId, content);
            String ai = chatClient.prompt()
                    .system(buildSystemPrompt())
                    .user(prompt)
                    .tools(assistantToolService)
                    .call()
                    .content();
            return parseAiResponse(ai);
        } catch (Exception e) {
            return reply("我刚刚没有理解成功，请换个说法再试一次。");
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
                + "最终只返回 JSON，格式: {\"reply\":\"...\",\"type\":\"text|order_list|payment_status|goods_search\",\"items\":[],\"result\":{},\"order\":{}}。";
    }

    private static String buildUserPrompt(Long userId, String content) {
        return "当前登录用户ID: " + (userId == null ? "null" : userId) + "\n用户问题: " + content;
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
}
