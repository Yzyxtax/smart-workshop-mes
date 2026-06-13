package com.xtax.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息请求体
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {

    /** 消息内容（自然语言，最大 4000 字符） */
    private String content;
}
