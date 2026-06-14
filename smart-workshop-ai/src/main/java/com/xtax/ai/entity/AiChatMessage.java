package com.xtax.ai.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatMessage {

    /** 主键 */
    private Long id;

    /** 所属会话 ID */
    private Long sessionId;

    /** 消息角色：user / assistant / system / tool */
    private String role;

    /** 消息内容（Markdown 格式，tool 角色时为 JSON） */
    private String content;

    /** AI 请求的工具调用列表 [{name, params, result}] */
    private String toolCalls;

    /** token 消耗 {prompt, completion, total} */
    private String tokenUsage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
