package com.xtax.ai.agent;

import com.xtax.ai.dto.SendMessageRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI Agent 调度器接口。
 * 负责一次 AI 消息交互的完整编排：
 * 组装上下文 → 调用 LLM → 解析工具调用 → 执行 → 返回结果 → 写入审计
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface AiAgentService {

    /**
     * 处理用户消息，通过 SseEmitter 流式推送响应
     *
     * @param sessionId 会话 ID
     * @param message   用户消息（含 content）
     * @param emitter   SSE 发射器，用于向前端推送流式事件
     */
    void process(Long sessionId, SendMessageRequest message, SseEmitter emitter);
}
