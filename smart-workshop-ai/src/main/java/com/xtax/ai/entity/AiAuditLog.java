package com.xtax.ai.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 操作审计日志实体
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiAuditLog {

    /** 主键 */
    private Long id;

    /** 关联会话 ID */
    private Long sessionId;

    /** 关联用户消息 ID */
    private Long messageId;

    /** 操作人（JWT 中的 userId） */
    private Integer userId;

    /** 调用的工具名称 */
    private String toolName;

    /** 工具参数（脱敏后的 JSON） */
    private String toolParams;

    /** 执行结果（成功时为返回值 JSON，失败时为错误信息） */
    private String executionResult;

    /** 操作目标类型（表名或实体名） */
    private String targetType;

    /** 操作目标 ID */
    private String targetId;

    /** 是否成功 */
    private Boolean success;

    /** 失败时的错误信息 */
    private String errorMessage;

    /** 执行耗时（毫秒） */
    private Integer durationMs;

    /** 用户原始自然语言输入 */
    private String userNaturalInput;

    /** 审计时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
