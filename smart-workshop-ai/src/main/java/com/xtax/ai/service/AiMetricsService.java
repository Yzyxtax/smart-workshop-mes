package com.xtax.ai.service;

import java.util.Map;

/**
 * AI 指标监控服务接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface AiMetricsService {

    /**
     * 获取 AI 模块运行指标快照
     *
     * @return 指标数据
     */
    Map<String, Object> getMetricsSnapshot();

    /**
     * 记录一次成功的 LLM 调用
     */
    void recordLlmSuccess(long latencyMs, long ttftMs, long promptTokens, long completionTokens);

    /**
     * 记录一次失败的 LLM 调用
     */
    void recordLlmFailure(String errorType);

    /**
     * 记录一次工具调用
     */
    void recordToolCall(String toolName);
}
