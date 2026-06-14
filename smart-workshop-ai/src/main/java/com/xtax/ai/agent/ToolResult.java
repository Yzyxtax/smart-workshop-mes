package com.xtax.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行结果值对象。
 * 封装工具执行后的返回值，包含成功/失败状态、数据和自然语言消息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    /** 是否执行成功 */
    private boolean success;

    /** 返回数据（成功时） */
    private Object data;

    /** 错误信息（失败时） */
    private String errorMessage;

    /** 是否需要用户二次确认 */
    @Builder.Default
    private boolean needsConfirmation = false;

    /** 确认提示文本 */
    private String confirmationMessage;

    // ========== 工厂方法 ==========

    /**
     * 创建成功结果
     */
    public static ToolResult success(Object data) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 创建成功结果（无数据）
     */
    public static ToolResult success() {
        return ToolResult.builder()
                .success(true)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ToolResult error(String errorMessage) {
        return ToolResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建需要二次确认的结果
     */
    public static ToolResult needsConfirmation(String message) {
        return ToolResult.builder()
                .success(false)
                .needsConfirmation(true)
                .confirmationMessage(message)
                .errorMessage(message)
                .build();
    }

    /**
     * 将结果转换为 LLM 可读的 Map 格式
     */
    public Map<String, Object> toMap() {
        if (success) {
            return Map.of("success", true, "data", data != null ? data : "操作成功");
        } else if (needsConfirmation) {
            return Map.of("success", false, "needsConfirmation", true,
                    "message", confirmationMessage != null ? confirmationMessage : "需要用户确认");
        } else {
            return Map.of("success", false, "error", errorMessage != null ? errorMessage : "未知错误");
        }
    }
}
