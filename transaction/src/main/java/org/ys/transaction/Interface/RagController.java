package org.ys.transaction.Interface;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.RagKnowledgeApplicationService;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识库 RAG：写入与问答。生产环境请接入网关鉴权与限流，本模块不重复实现安全框架。
 */
@RestController
@RequestMapping("rag/knowledge")
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true", matchIfMissing = true)
public class RagController {

    @Resource
    private RagKnowledgeApplicationService ragKnowledgeApplicationService;

    @PostMapping("ingest")
    @ResponseBody
    public CommentResult ingest(@RequestBody Map<String, Object> body) {
        try {
            String docKey = body.get("documentKey") == null ? null : body.get("documentKey").toString().trim();
            String title = body.get("title") == null ? null : body.get("title").toString();
            String content = body.get("content") == null ? null : body.get("content").toString();
            boolean replace = Boolean.TRUE.equals(body.get("replace"))
                    || "true".equalsIgnoreCase(String.valueOf(body.get("replace")));
            return CommentResult.success(ragKnowledgeApplicationService.ingestDocument(docKey, title, content, replace));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("query")
    @ResponseBody
    public CommentResult query(@RequestBody Map<String, Object> body) {
        try {
            String q = body.get("query") == null ? null : body.get("query").toString();
            Integer topK = null;
            if (body.get("topK") != null) {
                topK = Integer.valueOf(body.get("topK").toString());
            }
            Double minScore = null;
            if (body.get("minScore") != null) {
                minScore = Double.valueOf(body.get("minScore").toString());
            }
            return CommentResult.success(ragKnowledgeApplicationService.queryKnowledge(q, topK, minScore));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("delete")
    @ResponseBody
    public CommentResult delete(@RequestBody Map<String, Object> body) {
        try {
            String docKey = body.get("documentKey") == null ? null : body.get("documentKey").toString().trim();
            ragKnowledgeApplicationService.deleteDocument(docKey);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", Boolean.TRUE);
            payload.put("documentKey", docKey);
            return CommentResult.success(payload);
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}
