package com.xtax.ai.agent;

import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.ai.agent.AiToolMeta.ToolParamMeta;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表。
 * 启动时扫描所有 @AiTool 注解的 Bean，自动构建工具注册表和 JSON Schema。
 * <p>
 * 工具发现机制：扫描 Spring 容器中所有带 @AiTool 注解且实现 ToolHandler 的 Bean。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
public class ToolRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /** toolName → ToolHandler 映射 */
    private final Map<String, ToolHandler> handlers = new ConcurrentHashMap<>();

    /** toolName → AiToolMeta 映射 */
    private final Map<String, AiToolMeta> metas = new ConcurrentHashMap<>();

    /** 发送给 LLM 的 JSON Schema 定义列表（缓存） */
    private volatile List<Map<String, Object>> cachedDefinitions;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动时扫描所有 @AiTool Bean，构建工具注册表
     */
    @PostConstruct
    public void scan() {
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(AiTool.class);

        for (Map.Entry<String, Object> entry : toolBeans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
            AiTool annotation = beanClass.getAnnotation(AiTool.class);

            if (!(bean instanceof ToolHandler)) {
                log.warn("@AiTool 标注的类 {} 未实现 ToolHandler 接口，跳过", beanClass.getName());
                continue;
            }

            String toolName = annotation.name();
            ToolHandler handler = (ToolHandler) bean;

            // 反射提取 @ToolParam 字段构建 ToolParamMeta
            List<ToolParamMeta> paramMetas = extractParamMetas(beanClass);

            AiToolMeta meta = AiToolMeta.builder()
                    .name(toolName)
                    .label(annotation.label().isEmpty() ? toolName : annotation.label())
                    .description(annotation.description())
                    .category(annotation.category())
                    .permissions(annotation.permissions())
                    .requiresConfirmation(annotation.requiresConfirmation())
                    .handlerClass(beanClass.getName())
                    .params(paramMetas)
                    .build();

            handlers.put(toolName, handler);
            metas.put(toolName, meta);
            log.info("注册 AI 工具: {} → {}", toolName, beanClass.getSimpleName());
        }

        // 构建 LLM 可用的 JSON Schema 列表
        buildDefinitions();
        log.info("AI 工具注册完成，共 {} 个工具", handlers.size());
    }

    /**
     * 反射提取工具类中 @ToolParam 注解的字段元信息
     */
    private List<ToolParamMeta> extractParamMetas(Class<?> beanClass) {
        List<ToolParamMeta> paramMetas = new ArrayList<>();
        for (Field field : beanClass.getDeclaredFields()) {
            ToolParam paramAnno = field.getAnnotation(ToolParam.class);
            if (paramAnno == null) {
                continue;
            }
            String jsonType = mapJavaTypeToJsonType(field.getType());
            paramMetas.add(ToolParamMeta.builder()
                    .name(field.getName())
                    .description(paramAnno.description())
                    .required(paramAnno.required())
                    .jsonType(jsonType)
                    .enumValues(paramAnno.enumValues().length > 0 ? paramAnno.enumValues() : null)
                    .build());
        }
        return paramMetas;
    }

    /**
     * 将 Java 类型映射为 JSON Schema 类型
     */
    private String mapJavaTypeToJsonType(Class<?> javaType) {
        if (javaType == String.class) return "string";
        if (javaType == Integer.class || javaType == int.class
                || javaType == Long.class || javaType == long.class) return "integer";
        if (javaType == Double.class || javaType == double.class
                || javaType == Float.class || javaType == float.class) return "number";
        if (javaType == Boolean.class || javaType == boolean.class) return "boolean";
        if (javaType == List.class || javaType.isArray()) return "array";
        return "string"; // 默认
    }

    /**
     * 构建发送给 LLM 的 JSON Schema 工具定义列表
     */
    private void buildDefinitions() {
        List<Map<String, Object>> defs = new ArrayList<>();
        for (AiToolMeta meta : metas.values()) {
            Map<String, Object> toolDef = new LinkedHashMap<>();
            toolDef.put("name", meta.getName());
            toolDef.put("description", meta.getDescription());

            Map<String, Object> inputSchema = new LinkedHashMap<>();
            inputSchema.put("type", "object");

            Map<String, Object> properties = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();

            for (ToolParamMeta param : meta.getParams()) {
                Map<String, Object> prop = new LinkedHashMap<>();
                prop.put("type", param.getJsonType());
                prop.put("description", param.getDescription());
                if (param.getEnumValues() != null && param.getEnumValues().length > 0) {
                    prop.put("enum", Arrays.asList(param.getEnumValues()));
                }
                properties.put(param.getName(), prop);

                if (param.isRequired()) {
                    required.add(param.getName());
                }
            }

            inputSchema.put("properties", properties);
            if (!required.isEmpty()) {
                inputSchema.put("required", required);
            }
            toolDef.put("input_schema", inputSchema);

            defs.add(toolDef);
        }
        this.cachedDefinitions = defs;
    }

    /**
     * 获取发送给 LLM 的工具定义列表
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return cachedDefinitions != null ? cachedDefinitions : Collections.emptyList();
    }

    /**
     * 获取工具处理器
     */
    public ToolHandler getHandler(String toolName) {
        return handlers.get(toolName);
    }

    /**
     * 获取工具元信息
     */
    public AiToolMeta getMeta(String toolName) {
        return metas.get(toolName);
    }

    /**
     * 获取所有已注册的工具名称
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(handlers.keySet());
    }

    /**
     * 获取已注册工具数量
     */
    public int getToolCount() {
        return handlers.size();
    }
}
