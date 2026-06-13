package com.xtax.ai.agent;

import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * ToolRegistry 注册表单元测试。
 * 验证 AI 工具扫描、元数据提取和 JSON Schema 构建逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("ToolRegistry 注册表测试")
@ExtendWith(MockitoExtension.class)
class ToolRegistryTest {

    @Mock
    private ApplicationContext applicationContext;

    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        registry.setApplicationContext(applicationContext);
    }

    // ==================== 辅助：测试用 ToolHandler ====================

    @AiTool(
        name = "test_search",
        description = "测试搜索工具",
        category = "测试",
        label = "测试搜索"
    )
    static class TestSearchTool implements ToolHandler {
        @ToolParam(description = "搜索关键词", required = true)
        private String keyword;

        @ToolParam(description = "页码", required = false)
        private Integer page;

        @Override
        public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
            return ToolResult.success(Map.of("found", 5));
        }
    }

    @AiTool(
        name = "test_delete",
        description = "测试删除工具，需要确认",
        category = "测试",
        permissions = {"SYS_ADMIN"},
        requiresConfirmation = true,
        label = "测试删除"
    )
    static class TestDeleteTool implements ToolHandler {
        @ToolParam(description = "目标ID", required = true)
        private Long targetId;

        @Override
        public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
            return ToolResult.success("deleted");
        }
    }

    // ==================== scan() 测试 ====================

    @Nested
    @DisplayName("scan() 方法")
    class ScanMethod {

        @Test
        @DisplayName("应正确扫描 @AiTool Bean 并注册 Handler 和 Meta")
        void shouldRegisterAnnotatedBeans() {
            Map<String, Object> beans = new LinkedHashMap<>();
            beans.put("testSearchTool", new TestSearchTool());
            beans.put("testDeleteTool", new TestDeleteTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);

            registry.scan();

            assertEquals(2, registry.getToolCount());
            assertTrue(registry.getToolNames().contains("test_search"));
            assertTrue(registry.getToolNames().contains("test_delete"));
        }

        @Test
        @DisplayName("应跳过未实现 ToolHandler 的 @AiTool Bean")
        void shouldSkipNonToolHandlerBeans() {
            @AiTool(name = "bad_tool", description = "未实现接口", category = "测试")
            class BadTool {
            }

            Map<String, Object> beans = Map.of(
                "goodTool", new TestSearchTool(),
                "badTool", new BadTool()
            );
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);

            registry.scan();

            assertEquals(1, registry.getToolCount(),
                "应只注册 1 个有效工具，跳过未实现 ToolHandler 的 Bean");
        }

        @Test
        @DisplayName("空容器扫描后工具数应为 0")
        void shouldResultInZeroToolsForEmptyContext() {
            when(applicationContext.getBeansWithAnnotation(AiTool.class))
                .thenReturn(Collections.emptyMap());

            registry.scan();

            assertEquals(0, registry.getToolCount());
            assertTrue(registry.getToolNames().isEmpty());
        }
    }

    // ==================== Handler/Meta 查询测试 ====================

    @Nested
    @DisplayName("查询方法")
    class LookupMethods {

        @BeforeEach
        void registerTestTool() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();
        }

        @Test
        @DisplayName("getHandler() 应返回已注册的处理器")
        void shouldReturnRegisteredHandler() {
            ToolHandler handler = registry.getHandler("test_search");
            assertNotNull(handler);
            assertInstanceOf(TestSearchTool.class, handler);
        }

        @Test
        @DisplayName("getHandler() 查询未注册工具名应返回 null")
        void shouldReturnNullForUnknownTool() {
            assertNull(registry.getHandler("nonexistent"));
        }

        @Test
        @DisplayName("getMeta() 应返回已注册工具的元信息")
        void shouldReturnToolMeta() {
            AiToolMeta meta = registry.getMeta("test_search");
            assertNotNull(meta);
            assertEquals("test_search", meta.getName());
            assertEquals("测试搜索工具", meta.getDescription());
            assertEquals("测试", meta.getCategory());
            assertEquals("测试搜索", meta.getLabel());
        }

        @Test
        @DisplayName("getMeta() 查询未注册工具名应返回 null")
        void shouldReturnNullMetaForUnknownTool() {
            assertNull(registry.getMeta("unknown_tool"));
        }

        @Test
        @DisplayName("getToolNames() 应返回不可修改的集合")
        void shouldReturnUnmodifiableSet() {
            Set<String> names = registry.getToolNames();
            assertThat(names).containsExactly("test_search");
            assertThrows(UnsupportedOperationException.class, () -> names.add("new_tool"));
        }
    }

    // ==================== JSON Schema 构建测试 ====================

    @Nested
    @DisplayName("getToolDefinitions() JSON Schema")
    class ToolDefinitions {

        @Test
        @DisplayName("生成的 JSON Schema 应包含 name、description 和 input_schema")
        void shouldContainNameDescriptionAndSchema() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            List<Map<String, Object>> defs = registry.getToolDefinitions();
            assertEquals(1, defs.size());

            Map<String, Object> def = defs.get(0);
            assertEquals("test_search", def.get("name"));
            assertEquals("测试搜索工具", def.get("description"));
            assertThat(def).containsKey("input_schema");
        }

        @Test
        @DisplayName("input_schema 应包含 type=object 和 properties")
        void shouldHaveObjectTypeAndProperties() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            List<Map<String, Object>> defs = registry.getToolDefinitions();
            @SuppressWarnings("unchecked")
            Map<String, Object> inputSchema = (Map<String, Object>) defs.get(0).get("input_schema");

            assertEquals("object", inputSchema.get("type"));
            assertThat(inputSchema).containsKey("properties");
            assertThat(inputSchema).containsKey("required");
        }

        @Test
        @DisplayName("必填参数应出现在 required 数组中")
        void shouldIncludeRequiredParams() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            List<Map<String, Object>> defs = registry.getToolDefinitions();
            @SuppressWarnings("unchecked")
            Map<String, Object> inputSchema = (Map<String, Object>) defs.get(0).get("input_schema");
            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) inputSchema.get("required");

            assertThat(required).contains("keyword");
            assertThat(required).doesNotContain("page");
        }

        @Test
        @DisplayName("参数应包含 type 和 description")
        void shouldContainParamTypeAndDescription() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            List<Map<String, Object>> defs = registry.getToolDefinitions();
            @SuppressWarnings("unchecked")
            Map<String, Object> props =
                (Map<String, Object>) ((Map<String, Object>) defs.get(0).get("input_schema"))
                    .get("properties");
            @SuppressWarnings("unchecked")
            Map<String, Object> keywordProp = (Map<String, Object>) props.get("keyword");

            assertEquals("string", keywordProp.get("type"));
            assertEquals("搜索关键词", keywordProp.get("description"));
        }

        @Test
        @DisplayName("Integer 参数应映射为 integer 类型")
        void shouldMapIntegerToJsonInteger() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            List<Map<String, Object>> defs = registry.getToolDefinitions();
            @SuppressWarnings("unchecked")
            Map<String, Object> props =
                (Map<String, Object>) ((Map<String, Object>) defs.get(0).get("input_schema"))
                    .get("properties");
            @SuppressWarnings("unchecked")
            Map<String, Object> pageProp = (Map<String, Object>) props.get("page");

            assertEquals("integer", pageProp.get("type"));
        }
    }

    // ==================== 元数据正确性测试 ====================

    @Nested
    @DisplayName("元数据提取正确性")
    class MetaCorrectness {

        @Test
        @DisplayName("应正确提取 requiresConfirmation=true 的工具")
        void shouldExtractRequiresConfirmation() {
            Map<String, Object> beans = Map.of("testDeleteTool", new TestDeleteTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            AiToolMeta meta = registry.getMeta("test_delete");
            assertTrue(meta.isRequiresConfirmation());
        }

        @Test
        @DisplayName("应正确提取权限要求的工具")
        void shouldExtractPermissions() {
            Map<String, Object> beans = Map.of("testDeleteTool", new TestDeleteTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            AiToolMeta meta = registry.getMeta("test_delete");
            assertThat(meta.getPermissions()).containsExactly("SYS_ADMIN");
        }

        @Test
        @DisplayName("应正确提取 handlerClass 名")
        void shouldExtractHandlerClassName() {
            Map<String, Object> beans = Map.of("testSearchTool", new TestSearchTool());
            when(applicationContext.getBeansWithAnnotation(AiTool.class)).thenReturn(beans);
            registry.scan();

            AiToolMeta meta = registry.getMeta("test_search");
            assertEquals(TestSearchTool.class.getName(), meta.getHandlerClass());
        }
    }

    // ==================== 未扫描状态测试 ====================

    @Nested
    @DisplayName("未扫描状态")
    class UnscannedState {

        @Test
        @DisplayName("未扫描时 getToolDefinitions() 应返回空列表而非 null")
        void shouldReturnEmptyListWhenUnscanned() {
            List<Map<String, Object>> defs = registry.getToolDefinitions();
            assertNotNull(defs);
            assertTrue(defs.isEmpty());
        }

        @Test
        @DisplayName("未扫描时 getToolCount() 应返回 0")
        void shouldReturnZeroCountWhenUnscanned() {
            assertEquals(0, registry.getToolCount());
        }
    }
}
