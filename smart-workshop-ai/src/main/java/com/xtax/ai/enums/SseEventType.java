package com.xtax.ai.enums;

import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 事件类型枚举。
 * 定义前端 SSE 流中所有可能的事件类型。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Getter
public enum SseEventType {

    /** AI 正在思考 */
    THINKING("thinking", "AI 正在思考"),

    /** 流式文本片段 */
    TEXT_DELTA("text_delta", "文本片段"),

    /** LLM 请求调用工具 */
    TOOL_CALL("tool_call", "工具调用"),

    /** 工具执行结果 */
    TOOL_RESULT("tool_result", "工具结果"),

    /** 对话完成 */
    DONE("done", "完成"),

    /** 发生错误 */
    ERROR("error", "错误");

    private final String eventName;
    private final String desc;

    SseEventType(String eventName, String desc) {
        this.eventName = eventName;
        this.desc = desc;
    }

    /**
     * 构建 SseEmitter.SseEventBuilder 事件对象
     *
     * @param data 事件数据
     * @return SSE 事件构建器
     */
    public SseEmitter.SseEventBuilder toEvent(Object data) {
        return SseEmitter.event()
                .name(this.eventName)
                .data(data, MediaType.APPLICATION_JSON);
    }

    /**
     * 构建无数据的 SSE 事件
     */
    public SseEmitter.SseEventBuilder toEvent() {
        return SseEmitter.event()
                .name(this.eventName)
                .data("{}", MediaType.APPLICATION_JSON);
    }
}
