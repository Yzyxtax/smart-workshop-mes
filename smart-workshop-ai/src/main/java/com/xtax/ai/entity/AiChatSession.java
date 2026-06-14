package com.xtax.ai.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话会话实体
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatSession {

    /** 主键 */
    private Long id;

    /** 会话归属用户 ID */
    private Integer userId;

    /** 会话标题（首条消息前 50 字符自动生成） */
    private String title;

    /** 会话状态：ACTIVE / ARCHIVED */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 最后活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
