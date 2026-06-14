package com.xtax.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行上下文。
 * 封装一次工具调用所需的用户、会话、权限等运行时信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolContext {

    /** 操作人用户 ID */
    private Integer userId;

    /** 操作人用户名 */
    private String userName;

    /** 当前会话 ID */
    private Long sessionId;

    /** 当前消息 ID */
    private Long messageId;

    /** 用户原始自然语言输入 */
    private String userNaturalInput;

    /** 用户是否已确认（破坏性操作二次确认） */
    @Builder.Default
    private boolean confirmed = false;

    /** 用户权限编码集合 */
    private java.util.Set<String> permissionCodes;

    /** 额外扩展属性 */
    private Map<String, Object> extra;
}
