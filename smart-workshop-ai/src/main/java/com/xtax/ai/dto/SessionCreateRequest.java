package com.xtax.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建会话请求体
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreateRequest {

    /** 会话标题（可选，不传则首条消息自动生成） */
    private String title;
}
