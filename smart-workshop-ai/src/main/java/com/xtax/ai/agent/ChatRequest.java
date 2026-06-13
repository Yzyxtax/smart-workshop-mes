package com.xtax.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求值对象。
 * 封装发送给 LLM 的完整请求信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /** 格式化后的消息列表 [{role, content}, ...] */
    private List<Map<String, Object>> messages;

    /** 工具定义列表（JSON Schema） */
    private List<Map<String, Object>> tools;

    /** 最大输出 token 数 */
    private Integer maxTokens;

    /** 生成温度 */
    private Double temperature;

    /** 模型名称 */
    private String model;

    /** 是否流式输出 */
    @Builder.Default
    private Boolean stream = true;
}
