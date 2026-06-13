package com.xtax.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 事件值对象。
 * 统一的 SSE 事件模型，屏蔽不同 LLM 提供商的差异。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEvent {

    /** 事件类型 */
    private EventType type;

    /** 事件数据（文本片段 / 工具调用详情 / token 统计等） */
    private Object data;

    /** 工具调用列表（TOOL_USE 时非空） */
    private List<ToolUse> toolUses;

    /** Token 消耗统计（TEXT_COMPLETE 时非空） */
    private Map<String, Object> tokenUsage;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /** 流式文本片段 */
        TEXT_DELTA,
        /** LLM 决定调用工具 */
        TOOL_USE,
        /** LLM 文本回复完成 */
        TEXT_COMPLETE,
        /** 发生错误 */
        ERROR
    }

    /**
     * 工具调用详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolUse {
        /** 工具调用唯一 ID */
        private String id;
        /** 工具名称 */
        private String name;
        /** 工具参数 */
        private Map<String, Object> params;
    }
}
