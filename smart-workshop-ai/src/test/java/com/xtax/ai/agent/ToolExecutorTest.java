package com.xtax.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtax.ai.config.AiProperties;
import com.xtax.ai.config.AiProperties.RateLimitConfig;
import com.xtax.ai.mapper.AiAuditLogMapper;
import com.xtax.ai.service.AiMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ToolExecutor 沙箱管道单元测试。
 * 按 7 步执行流程逐项验证每条安全防线的正确性。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("ToolExecutor 沙箱管道测试")
@ExtendWith(MockitoExtension.class)
class ToolExecutorTest {

    @Mock
    private ToolRegistry toolRegistry;

    @Mock
    private AiAuditLogMapper auditLogMapper;

    @Mock
    private AiMetricsService metricsService;

    @Mock
    private ToolExecutor.PermissionChecker permissionChecker;

    private AiProperties aiProperties;
    private ToolExecutor executor;

    /** 测试上下文 */
    private final ToolContext ctx = ToolContext.builder()
            .userId(1001)
            .userName("测试用户")
            .sessionId(1L)
            .messageId(10L)
            .userNaturalInput("帮我搜索设备A")
            .confirmed(false)
            .build();

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setRateLimit(new RateLimitConfig());
        aiProperties.getRateLimit().setMaxToolCallsPerMinute(10);

        executor = new ToolExecutor(
                toolRegistry, auditLogMapper, metricsService,
                aiProperties, new ObjectMapper()
        );
        executor.setPermissionChecker(permissionChecker);
    }

    /** 工具元信息辅助方法 */
    private AiToolMeta buildMeta(String name, String desc, String[] permissions,
                                  boolean requiresConfirmation,
                                  AiToolMeta.ToolParamMeta... params) {
        return AiToolMeta.builder()
                .name(name)
                .label(desc)
                .description(desc)
                .category("测试")
                .permissions(permissions)
                .requiresConfirmation(requiresConfirmation)
                .handlerClass(TestHandler.class.getName())
                .params(java.util.Arrays.asList(params))
                .build();
    }

    /** 参数元信息辅助方法 */
    private AiToolMeta.ToolParamMeta buildParam(String name, String desc,
                                                 boolean required, String jsonType) {
        return AiToolMeta.ToolParamMeta.builder()
                .name(name).description(desc).required(required)
                .jsonType(jsonType).build();
    }

    /** 带枚举的参数 */
    private AiToolMeta.ToolParamMeta buildEnumParam(String name, String desc,
                                                     boolean required, String[] enumValues) {
        return AiToolMeta.ToolParamMeta.builder()
                .name(name).description(desc).required(required)
                .jsonType("string").enumValues(enumValues).build();
    }

    /** 测试用的简单 Handler */
    static class TestHandler implements ToolHandler {
        @Override
        public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
            return ToolResult.success(Map.of("result", "ok"));
        }
    }

    // ==================== Step 1: 工具查找 ====================

    @Nested
    @DisplayName("Step 1 - 工具查找")
    class Step1ToolLookup {

        @Test
        @DisplayName("未知工具名应返回错误")
        void shouldFailForUnknownTool() {
            when(toolRegistry.getHandler("unknown_tool")).thenReturn(null);

            ToolResult result = executor.execute("unknown_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("不支持的操作");
        }

        @Test
        @DisplayName("已注册工具应正常执行")
        void shouldProceedForKnownTool() {
            TestHandler handler = new TestHandler();
            AiToolMeta meta = buildMeta("known_tool", "已知工具", null, false);
            when(toolRegistry.getHandler("known_tool")).thenReturn(handler);
            when(toolRegistry.getMeta("known_tool")).thenReturn(meta);

            ToolResult result = executor.execute("known_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
            verify(metricsService).recordToolCall("known_tool");
            verify(auditLogMapper).insert(any());
        }
    }

    // ==================== Step 2: 参数校验 ====================

    @Nested
    @DisplayName("Step 2 - 参数校验")
    class Step2ParamValidation {

        @Test
        @DisplayName("缺少必填参数应返回校验错误")
        void shouldFailForMissingRequiredParam() {
            AiToolMeta meta = buildMeta("tool_with_param", "含参数工具", null, false,
                    buildParam("keyword", "关键词", true, "string"));
            when(toolRegistry.getHandler("tool_with_param")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("tool_with_param")).thenReturn(meta);

            ToolResult result = executor.execute("tool_with_param", Map.of("other", "x"), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("keyword");
            assertThat(result.getErrorMessage()).contains("必填参数");
        }

        @Test
        @DisplayName("空字符串必填参数应视为缺失")
        void shouldTreatEmptyStringAsMissing() {
            AiToolMeta meta = buildMeta("tool_with_param", "含参数工具", null, false,
                    buildParam("keyword", "关键词", true, "string"));
            when(toolRegistry.getHandler("tool_with_param")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("tool_with_param")).thenReturn(meta);

            ToolResult result = executor.execute("tool_with_param",
                    Map.of("keyword", ""), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("必填参数");
        }

        @Test
        @DisplayName("枚举参数值不在允许范围内应返回校验错误")
        void shouldFailForInvalidEnumValue() {
            AiToolMeta meta = buildMeta("enum_tool", "枚举工具", null, false,
                    buildEnumParam("status", "状态", true,
                            new String[]{"CREATED", "RUNNING", "COMPLETED"}));
            when(toolRegistry.getHandler("enum_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("enum_tool")).thenReturn(meta);

            ToolResult result = executor.execute("enum_tool",
                    Map.of("status", "INVALID_STATUS"), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("不在允许范围内");
        }

        @Test
        @DisplayName("有效枚举参数值应通过校验")
        void shouldAllowValidEnumValue() {
            AiToolMeta meta = buildMeta("enum_tool", "枚举工具", null, false,
                    buildEnumParam("status", "状态", true,
                            new String[]{"CREATED", "RUNNING", "COMPLETED"}));
            when(toolRegistry.getHandler("enum_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("enum_tool")).thenReturn(meta);

            ToolResult result = executor.execute("enum_tool",
                    Map.of("status", "RUNNING"), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("可选参数缺失应正常通过")
        void shouldAllowMissingOptionalParam() {
            AiToolMeta meta = buildMeta("opt_tool", "可选参数工具", null, false,
                    buildParam("keyword", "关键词", true, "string"),
                    buildParam("filter", "过滤条件", false, "string"));
            when(toolRegistry.getHandler("opt_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("opt_tool")).thenReturn(meta);

            ToolResult result = executor.execute("opt_tool",
                    Map.of("keyword", "test"), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("null 参数值应通过校验（可选参数允许 null）")
        void shouldAllowNullValueForOptionalParam() {
            AiToolMeta meta = buildMeta("opt_tool", "可选参数工具", null, false,
                    buildParam("keyword", "关键词", true, "string"),
                    buildParam("filter", "过滤条件", false, "string"));
            when(toolRegistry.getHandler("opt_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("opt_tool")).thenReturn(meta);

            // Map.of() 不支持 null 值，使用 HashMap
            java.util.HashMap<String, Object> params = new java.util.HashMap<>();
            params.put("keyword", "test");
            params.put("filter", null);

            ToolResult result = executor.execute("opt_tool", params, ctx);

            assertTrue(result.isSuccess());
        }
    }

    // ==================== Step 3: RBAC 权限校验 ====================

    @Nested
    @DisplayName("Step 3 - RBAC 权限校验")
    class Step3PermissionCheck {

        @Test
        @DisplayName("无需权限的工具应跳过权限校验")
        void shouldSkipPermissionCheckWhenNoPermissions() {
            AiToolMeta meta = buildMeta("open_tool", "公开工具", null, false);
            when(toolRegistry.getHandler("open_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("open_tool")).thenReturn(meta);

            ToolResult result = executor.execute("open_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
            // 不应调用权限检查器
            verify(permissionChecker, never()).getUserPermissionCodes(any());
        }

        @Test
        @DisplayName("权限不足时应返回权限错误")
        void shouldFailForInsufficientPermissions() {
            String[] required = {"SYS_EQUIPMENT_MANAGE"};
            AiToolMeta meta = buildMeta("admin_tool", "管理员工具", required, false);
            when(toolRegistry.getHandler("admin_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("admin_tool")).thenReturn(meta);
            when(permissionChecker.getUserPermissionCodes(1001))
                    .thenReturn(Set.of("SYS_USER_VIEW"));

            ToolResult result = executor.execute("admin_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("SYS_EQUIPMENT_MANAGE");
            assertThat(result.getErrorMessage()).contains("权限");
        }

        @Test
        @DisplayName("权限充足时应正常执行")
        void shouldProceedWhenPermissionsSatisfied() {
            String[] required = {"SYS_EQUIPMENT_MANAGE"};
            AiToolMeta meta = buildMeta("admin_tool", "管理员工具", required, false);
            when(toolRegistry.getHandler("admin_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("admin_tool")).thenReturn(meta);
            when(permissionChecker.getUserPermissionCodes(1001))
                    .thenReturn(Set.of("SYS_EQUIPMENT_MANAGE", "SYS_USER_VIEW"));

            ToolResult result = executor.execute("admin_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("多权限同时满足时 ALL_MATCH 逻辑应正常")
        void shouldRequireAllPermissionsWhenMultiple() {
            String[] required = {"SYS_EQUIPMENT_MANAGE", "SYS_PROCESS_MANAGE"};
            AiToolMeta meta = buildMeta("super_tool", "超级工具", required, false);
            when(toolRegistry.getHandler("super_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("super_tool")).thenReturn(meta);
            // 只有部分权限
            when(permissionChecker.getUserPermissionCodes(1001))
                    .thenReturn(Set.of("SYS_EQUIPMENT_MANAGE"));

            ToolResult result = executor.execute("super_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("权限");
        }
    }

    // ==================== Step 4: 确认检查 ====================

    @Nested
    @DisplayName("Step 4 - 确认检查")
    class Step4Confirmation {

        @Test
        @DisplayName("需要确认但用户未确认时应返回 needsConfirmation 结果")
        void shouldReturnNeedsConfirmationWhenNotConfirmed() {
            AiToolMeta meta = buildMeta("delete_tool", "删除工具", null, true);
            when(toolRegistry.getHandler("delete_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("delete_tool")).thenReturn(meta);

            ToolResult result = executor.execute("delete_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertTrue(result.isNeedsConfirmation());
            assertThat(result.getConfirmationMessage()).contains("确认");
            // 确认检查在审计之前，未确认直接返回需求确认结果
        }

        @Test
        @DisplayName("需要确认且用户已确认时应正常执行")
        void shouldProceedWhenConfirmed() {
            AiToolMeta meta = buildMeta("delete_tool", "删除工具", null, true);
            when(toolRegistry.getHandler("delete_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("delete_tool")).thenReturn(meta);

            ToolContext confirmedCtx = ToolContext.builder()
                    .userId(1001).sessionId(1L).messageId(10L)
                    .confirmed(true).build();

            ToolResult result = executor.execute("delete_tool", Map.of(), confirmedCtx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("不需要确认的工具应跳过确认检查")
        void shouldSkipConfirmationWhenNotRequired() {
            AiToolMeta meta = buildMeta("read_tool", "只读工具", null, false);
            when(toolRegistry.getHandler("read_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("read_tool")).thenReturn(meta);

            ToolResult result = executor.execute("read_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
        }
    }

    // ==================== Step 5: 限流检查 ====================

    @Nested
    @DisplayName("Step 5 - 限流检查")
    class Step5RateLimit {

        @Test
        @DisplayName("未超过限流阈值时应正常执行")
        void shouldProceedUnderRateLimit() {
            aiProperties.getRateLimit().setMaxToolCallsPerMinute(100);
            AiToolMeta meta = buildMeta("quick_tool", "快速工具", null, false);
            when(toolRegistry.getHandler("quick_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("quick_tool")).thenReturn(meta);

            // 连续多次调用应在限流阈值内
            for (int i = 0; i < 10; i++) {
                ToolResult result = executor.execute("quick_tool", Map.of(), ctx);
                assertTrue(result.isSuccess(), "第 " + (i + 1) + " 次调用应成功");
            }
        }

        @Test
        @DisplayName("超过限流阈值时应返回限流错误")
        void shouldFailWhenOverRateLimit() {
            aiProperties.getRateLimit().setMaxToolCallsPerMinute(2);
            AiToolMeta meta = buildMeta("quick_tool", "快速工具", null, false);
            when(toolRegistry.getHandler("quick_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("quick_tool")).thenReturn(meta);

            // 执行 2 次成功
            executor.execute("quick_tool", Map.of(), ctx);
            executor.execute("quick_tool", Map.of(), ctx);
            // 第 3 次应超过限流
            ToolResult result = executor.execute("quick_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("频繁");
        }
    }

    // ==================== Step 6: 业务执行 ====================

    @Nested
    @DisplayName("Step 6 - 业务执行")
    class Step6BusinessExecution {

        @Test
        @DisplayName("Handler 返回成功结果时应透传")
        void shouldPassThroughHandlerSuccess() {
            AiToolMeta meta = buildMeta("success_tool", "成功工具", null, false);
            when(toolRegistry.getHandler("success_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("success_tool")).thenReturn(meta);

            ToolResult result = executor.execute("success_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsEntry("result", "ok");
        }

        @Test
        @DisplayName("Handler 返回错误结果时应透传错误信息")
        void shouldPassThroughHandlerError() {
            ToolHandler errorHandler = (params, c) ->
                    ToolResult.error("业务执行失败：数据不存在");
            AiToolMeta meta = buildMeta("error_tool", "错误工具", null, false);
            when(toolRegistry.getHandler("error_tool")).thenReturn(errorHandler);
            when(toolRegistry.getMeta("error_tool")).thenReturn(meta);

            ToolResult result = executor.execute("error_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertEquals("业务执行失败：数据不存在", result.getErrorMessage());
        }

        @Test
        @DisplayName("Handler 抛出异常时应被捕获并记录")
        void shouldCatchHandlerException() {
            ToolHandler throwingHandler = (params, c) -> {
                throw new RuntimeException("内部错误");
            };
            AiToolMeta meta = buildMeta("throw_tool", "异常工具", null, false);
            when(toolRegistry.getHandler("throw_tool")).thenReturn(throwingHandler);
            when(toolRegistry.getMeta("throw_tool")).thenReturn(meta);

            ToolResult result = executor.execute("throw_tool", Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertEquals("系统错误，请稍后重试", result.getErrorMessage());
        }
    }

    // ==================== Step 7: 审计记录 ====================

    @Nested
    @DisplayName("Step 7 - 审计记录")
    class Step7AuditLogging {

        @Test
        @DisplayName("成功执行时应写入成功审计记录")
        void shouldRecordSuccessAudit() {
            AiToolMeta meta = buildMeta("audit_tool", "审计工具", null, false);
            when(toolRegistry.getHandler("audit_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("audit_tool")).thenReturn(meta);

            executor.execute("audit_tool", Map.of(), ctx);

            ArgumentCaptor<com.xtax.ai.entity.AiAuditLog> captor =
                    ArgumentCaptor.forClass(com.xtax.ai.entity.AiAuditLog.class);
            verify(auditLogMapper).insert(captor.capture());

            com.xtax.ai.entity.AiAuditLog log = captor.getValue();
            assertEquals(ctx.getSessionId(), log.getSessionId());
            assertEquals(ctx.getUserId(), log.getUserId());
            assertEquals("audit_tool", log.getToolName());
            assertTrue(log.getSuccess());
        }

        @Test
        @DisplayName("失败执行时应写入失败审计记录")
        void shouldRecordFailureAudit() {
            AiToolMeta meta = buildMeta("fail_audit_tool", "失败审计", null, false);
            ToolHandler failHandler = (params, c) ->
                    ToolResult.error("数据库连接失败");
            when(toolRegistry.getHandler("fail_audit_tool")).thenReturn(failHandler);
            when(toolRegistry.getMeta("fail_audit_tool")).thenReturn(meta);

            executor.execute("fail_audit_tool", Map.of(), ctx);

            ArgumentCaptor<com.xtax.ai.entity.AiAuditLog> captor =
                    ArgumentCaptor.forClass(com.xtax.ai.entity.AiAuditLog.class);
            verify(auditLogMapper).insert(captor.capture());

            com.xtax.ai.entity.AiAuditLog log = captor.getValue();
            assertFalse(log.getSuccess());
            assertEquals("数据库连接失败", log.getErrorMessage());
        }

        @Test
        @DisplayName("审计记录应包含耗时信息")
        void shouldIncludeDurationInAuditLog() {
            AiToolMeta meta = buildMeta("duration_tool", "耗时工具", null, false);
            when(toolRegistry.getHandler("duration_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("duration_tool")).thenReturn(meta);

            executor.execute("duration_tool", Map.of(), ctx);

            ArgumentCaptor<com.xtax.ai.entity.AiAuditLog> captor =
                    ArgumentCaptor.forClass(com.xtax.ai.entity.AiAuditLog.class);
            verify(auditLogMapper).insert(captor.capture());
            assertNotNull(captor.getValue().getDurationMs());
            assertTrue(captor.getValue().getDurationMs() >= 0);
        }
    }

    // ==================== 边界情况 ====================

    @Nested
    @DisplayName("边界情况")
    class EdgeCases {

        @Test
        @DisplayName("PermissionChecker 未注入时，有权限的工具应跳过校验继续执行")
        void shouldSkipPermissionWhenCheckerNotSet() {
            executor.setPermissionChecker(null);
            String[] required = {"SYS_ADMIN"};
            AiToolMeta meta = buildMeta("admin_tool", "管理员工具", required, false);
            when(toolRegistry.getHandler("admin_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("admin_tool")).thenReturn(meta);

            ToolResult result = executor.execute("admin_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("权限数组为空时应等同无权限要求跳过校验")
        void shouldSkipPermissionWhenArrayEmpty() {
            AiToolMeta meta = buildMeta("open_tool", "公开工具", new String[0], false);
            when(toolRegistry.getHandler("open_tool")).thenReturn(new TestHandler());
            when(toolRegistry.getMeta("open_tool")).thenReturn(meta);

            ToolResult result = executor.execute("open_tool", Map.of(), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("每个步骤的错误信息应对用户清晰可读")
        void shouldHaveHumanReadableErrorMessages() {
            when(toolRegistry.getHandler("bad_tool")).thenReturn(null);

            ToolResult result = executor.execute("bad_tool", Map.of(), ctx);

            assertThat(result.getErrorMessage())
                    .isNotEmpty()
                    .doesNotContain("Exception")
                    .doesNotContain("StackTrace");
        }
    }
}
