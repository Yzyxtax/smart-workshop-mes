package com.xtax.ai.agent;

import reactor.core.publisher.Flux;

/**
 * LLM 提供商抽象接口。
 * 不同 LLM 提供商（Claude、OpenAI、私有化模型）通过实现此接口接入系统。
 * <p>
 * 使用 SSE 流式协议与 LLM 交互，返回统一的 AiEvent 事件流。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface AiProvider {

    /**
     * 流式调用 LLM，返回标准化事件流
     *
     * @param request 聊天请求（含消息历史、工具定义、参数配置）
     * @return 事件流（TEXT_DELTA | TOOL_USE | TEXT_COMPLETE | ERROR）
     */
    Flux<AiEvent> streamChat(ChatRequest request);

    /**
     * 判断此 Provider 是否支持指定的提供商标识
     *
     * @param provider 配置中的提供商标识（如 "claude"、"openai"）
     * @return true 表示本 Provider 可处理
     */
    boolean supports(String provider);
}
