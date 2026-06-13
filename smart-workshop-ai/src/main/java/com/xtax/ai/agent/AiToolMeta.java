package com.xtax.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工具元信息值对象。
 * 存储从 @AiTool 和 @ToolParam 注解中提取的工具元数据。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolMeta {

    /** 工具唯一名称 */
    private String name;

    /** 工具中文标签 */
    private String label;

    /** 工具描述 */
    private String description;

    /** 工具分类 */
    private String category;

    /** 所需权限编码列表 */
    private String[] permissions;

    /** 是否需要用户二次确认 */
    private boolean requiresConfirmation;

    /** 处理器类全限定名 */
    private String handlerClass;

    /** 参数元信息列表 */
    private List<ToolParamMeta> params;

    /**
     * 参数元信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolParamMeta {
        /** 参数名称 */
        private String name;
        /** 参数描述 */
        private String description;
        /** 是否必填 */
        private boolean required;
        /** JSON 类型 */
        private String jsonType;
        /** 枚举约束值 */
        private String[] enumValues;
    }
}
