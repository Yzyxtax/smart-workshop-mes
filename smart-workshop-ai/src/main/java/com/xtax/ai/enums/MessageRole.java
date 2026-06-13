package com.xtax.ai.enums;

import lombok.Getter;

/**
 * AI 对话消息角色枚举
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Getter
public enum MessageRole {

    /** 用户消息 */
    USER("user", "用户"),

    /** AI 助手回复 */
    ASSISTANT("assistant", "助手"),

    /** 系统提示 */
    SYSTEM("system", "系统"),

    /** 工具调用结果 */
    TOOL("tool", "工具");

    private final String code;
    private final String desc;

    MessageRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
