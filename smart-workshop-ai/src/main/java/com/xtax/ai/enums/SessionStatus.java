package com.xtax.ai.enums;

import lombok.Getter;

/**
 * AI 对话会话状态枚举
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Getter
public enum SessionStatus {

    /** 活跃中 */
    ACTIVE("ACTIVE", "活跃中"),

    /** 已归档 */
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String desc;

    SessionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
