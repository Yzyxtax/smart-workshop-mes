package com.xtax.ai.controller;

import com.xtax.ai.agent.AiAgentService;
import com.xtax.ai.dto.SendMessageRequest;
import com.xtax.ai.dto.SessionCreateRequest;
import com.xtax.ai.entity.AiChatMessage;
import com.xtax.ai.entity.AiChatSession;
import com.xtax.ai.service.AiSessionService;
import com.xtax.utils.JwtUtils;
import com.xtax.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天控制器。
 * 提供会话管理、SSE 消息发送等 REST 端点。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiSessionService sessionService;
    private final AiAgentService aiAgentService;

    /**
     * 创建新会话
     * POST /ai/chat/sessions
     */
    @PostMapping("/sessions")
    public Result createSession(HttpServletRequest request,
                                 @RequestBody(required = false) SessionCreateRequest body) {
        Integer userId = extractUserId(request);
        String title = body != null ? body.getTitle() : null;
        AiChatSession session = sessionService.createSession(userId, title);
        return Result.success(session);
    }

    /**
     * 查询会话列表（按活跃时间倒序，仅当前用户）
     * GET /ai/chat/sessions
     */
    @GetMapping("/sessions")
    public Result listSessions(HttpServletRequest request) {
        Integer userId = extractUserId(request);
        List<AiChatSession> sessions = sessionService.listSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 查询会话详情 + 全部消息历史
     * GET /ai/chat/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public Result getSession(HttpServletRequest request,
                              @PathVariable Long sessionId) {
        AiChatSession session = sessionService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        // 权限校验：只能查看自己的会话
        Integer userId = extractUserId(request);
        if (!userId.equals(session.getUserId())) {
            return Result.error(403, "无权查看此会话");
        }
        List<AiChatMessage> messages = sessionService.getSessionMessages(sessionId);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        data.put("messages", messages);
        return Result.success(data);
    }

    /**
     * 删除会话（级联删除所有消息）
     * DELETE /ai/chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result deleteSession(HttpServletRequest request,
                                 @PathVariable Long sessionId) {
        AiChatSession session = sessionService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        // 权限校验：只能删除自己的会话
        Integer userId = extractUserId(request);
        if (!userId.equals(session.getUserId())) {
            return Result.error(403, "无权删除此会话");
        }
        sessionService.deleteSession(sessionId);
        return Result.success();
    }

    /**
     * 发送消息（SSE 流式响应）★ 核心接口
     * POST /ai/chat/sessions/{sessionId}/messages
     */
    @PostMapping(value = "/sessions/{sessionId}/messages",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(HttpServletRequest request,
                                   @PathVariable Long sessionId,
                                   @RequestBody SendMessageRequest body) {
        // 参数校验
        if (body.getContent() == null || body.getContent().trim().isEmpty()) {
            SseEmitter errorEmitter = new SseEmitter(0L);
            errorEmitter.completeWithError(new IllegalArgumentException("消息内容不能为空"));
            return errorEmitter;
        }
        if (body.getContent().length() > 4000) {
            SseEmitter errorEmitter = new SseEmitter(0L);
            errorEmitter.completeWithError(new IllegalArgumentException("消息内容超过 4000 字符限制"));
            return errorEmitter;
        }

        // 校验会话存在
        AiChatSession session = sessionService.getSession(sessionId);
        if (session == null) {
            SseEmitter errorEmitter = new SseEmitter(0L);
            errorEmitter.completeWithError(new IllegalArgumentException("会话不存在"));
            return errorEmitter;
        }

        // ✅ 关键：在父线程（HTTP 工作线程）中预先提取用户信息，
        // 因为 AiAgentServiceImpl.process() 已改为 @Async 异步执行，
        // 子线程中无法再通过 RequestContextHolder 取到 HttpServletRequest。
        body.setUserId(extractUserId(request));
        body.setUserName(extractUserName(request));

        // 创建 SSE 发射器，超时时间 5 分钟
        SseEmitter emitter = new SseEmitter(300_000L);

        // 注册生命周期回调，避免静默丢失（出现问题时至少能在日志里看到）
        emitter.onTimeout(() -> {
            log.warn("SSE emitter 超时: sessionId={}", sessionId);
            emitter.complete();
        });
        emitter.onError(ex -> log.warn("SSE emitter 错误: sessionId={}, msg={}", sessionId, ex.getMessage()));
        emitter.onCompletion(() -> log.debug("SSE emitter 已完成: sessionId={}", sessionId));

        // 异步处理 AI 对话（不会阻塞当前线程，emitter 立即返回给 Spring）
        aiAgentService.process(sessionId, body, emitter);

        return emitter;
    }

    /**
     * 从 JWT Token 中提取用户 ID
     */
    private Integer extractUserId(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token != null) {
                Map<String, Object> claims = JwtUtils.parseToken(token);
                Object idObj = claims.get("id");
                if (idObj instanceof Integer) return (Integer) idObj;
                if (idObj instanceof Number) return ((Number) idObj).intValue();
            }
        } catch (Exception e) {
            log.warn("提取用户 ID 失败", e);
        }
        return 0;
    }

    /**
     * 从 JWT Token 中提取用户名
     */
    private String extractUserName(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token != null) {
                Map<String, Object> claims = JwtUtils.parseToken(token);
                Object nameObj = claims.get("name");
                if (nameObj != null) return nameObj.toString();
            }
        } catch (Exception e) {
            log.warn("提取用户名失败", e);
        }
        return "未知用户";
    }

    /**
     * 从请求中提取 JWT token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getHeader("token");
    }
}
