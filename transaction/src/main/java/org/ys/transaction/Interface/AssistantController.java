package org.ys.transaction.Interface;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.AssistantApplicationService;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("assistant")
public class AssistantController {

    @Resource
    private AssistantApplicationService assistantApplicationService;

    @RequestMapping("chat")
    @ResponseBody
    public CommentResult chat(@RequestBody Map<String, Object> body) {
        try {
            String message = body.get("message") == null ? "" : body.get("message").toString();
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String sessionId = body.get("sessionId") == null ? null : body.get("sessionId").toString();
            return CommentResult.success(assistantApplicationService.chat(userId, message, sessionId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 流式问答：SSE，事件 {@code delta} 携带增量文本 {@code {"t":"..."}}，最后一条 {@code done} 与 {@link #chat} 返回结构一致。
     */
    @PostMapping(value = "chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(300_000L);
        try {
            String message = body.get("message") == null ? "" : body.get("message").toString();
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String sessionId = body.get("sessionId") == null ? null : body.get("sessionId").toString();
            CompletableFuture.runAsync(() -> assistantApplicationService.streamChat(userId, message, sessionId,
                    delta -> {
                        try {
                            emitter.send(SseEmitter.event().name("delta").data(Map.of("t", delta)));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    finalPayload -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data(finalPayload));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @PostMapping("session/create")
    @ResponseBody
    public CommentResult createSession(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String title = body.get("title") == null ? null : body.get("title").toString();
            return CommentResult.success(assistantApplicationService.createSession(userId, title));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @GetMapping("session/list")
    @ResponseBody
    public CommentResult listSessions(@RequestParam(value = "userId", required = false) Long userId) {
        try {
            return CommentResult.success(assistantApplicationService.listSessions(userId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("session/delete")
    @ResponseBody
    public CommentResult deleteSession(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String sessionId = body.get("sessionId") == null ? null : body.get("sessionId").toString();
            return CommentResult.success(assistantApplicationService.deleteSession(userId, sessionId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @GetMapping("session/history")
    @ResponseBody
    public CommentResult sessionHistory(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam("sessionId") String sessionId
    ) {
        try {
            return CommentResult.success(assistantApplicationService.history(userId, sessionId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}
