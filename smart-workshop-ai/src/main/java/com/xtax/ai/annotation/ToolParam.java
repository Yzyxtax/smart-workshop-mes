package com.xtax.ai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具参数描述注解。
 * 标注在工具实现类的字段上，用于自动生成 JSON Schema 参数定义。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {

    /**
     * 参数描述，发送给 LLM 帮助正确填充
     */
    String description();

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 枚举约束值列表，如 {"CREATED", "RELEASED", "RUNNING"}
     */
    String[] enumValues() default {};
}
