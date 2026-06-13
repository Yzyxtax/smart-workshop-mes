package com.xtax.ai.agent;

import java.util.Map;

/**
 * 工具处理器接口。
 * 所有 AI 工具必须实现此接口，定义工具的执行逻辑。
 * <p>
 * 实现类需配合 @AiTool 和 @ToolParam 注解使用，
 * 由 ToolRegistry 在启动时自动扫描注册。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface ToolHandler {

    /**
     * 执行工具操作
     *
     * @param params LLM 填充的工具参数
     * @param ctx    工具执行上下文（用户、会话等信息）
     * @return 工具执行结果
     */
    ToolResult execute(Map<String, Object> params, ToolContext ctx);
}
