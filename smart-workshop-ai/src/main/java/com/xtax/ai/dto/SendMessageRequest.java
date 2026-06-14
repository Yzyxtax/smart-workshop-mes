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

    /**
     * 当前用户 ID（由 Controller 在 JWT 解析后注入，前端无需传入）。
     * <p>
     * 因为 process() 已改为 @Async 异步执行，子线程中无法再通过
     * RequestContextHolder 取到 HttpServletRequest，故必须由 Controller
     * 在父线程中提前抽取并以参数形式向下传递。
     */
    private Integer userId;

    /**
     * 当前用户名（由 Controller 注入）。
     */
    private String userName;
}
