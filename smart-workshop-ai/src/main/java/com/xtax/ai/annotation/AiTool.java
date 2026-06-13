package com.xtax.ai.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 工具标记注解。
 * 标注在工具实现类上，系统启动时自动扫描并注册到 ToolRegistry。
 * <p>
 * 被标注的类必须是 Spring Bean（@Component）且实现 ToolHandler 接口。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AiTool {

    /**
     * 工具唯一名称，对应 LLM Function Calling 的 function.name
     */
    String name();

    /**
     * 工具描述，发送给 LLM 用于意图匹配。应清晰说明工具的功能和使用场景
     */
    String description();

    /**
     * 工具分类（设备管理 / BOM管理 / 工序管理 ...）
     */
    String category() default "";

    /**
     * 执行此工具所需的权限编码列表。空数组表示无需特定权限
     */
    String[] permissions() default {};

    /**
     * 是否为破坏性操作，需要用户二次确认
     */
    boolean requiresConfirmation() default false;

    /**
     * 工具中文标签，用于审计日志和前端展示
     */
    String label() default "";
}
