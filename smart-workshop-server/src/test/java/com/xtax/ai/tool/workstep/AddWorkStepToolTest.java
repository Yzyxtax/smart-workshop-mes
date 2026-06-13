package com.xtax.ai.tool.workstep;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.WorkStep;
import com.xtax.service.workStepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AddWorkStepTool 单元测试。
 * 验证必填参数校验和工步创建逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("AddWorkStepTool 添加工步测试")
@ExtendWith(MockitoExtension.class)
class AddWorkStepToolTest {

    @Mock
    private workStepService workStepService;

    @InjectMocks
    private AddWorkStepTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    @Nested
    @DisplayName("参数校验")
    class ParamValidation {

        @Test
        @DisplayName("空 stepName 应返回错误")
        void shouldFailForEmptyStepName() {
            ToolResult result = tool.execute(Map.of("stepName", ""), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("缺少必填参数");
        }

        @Test
        @DisplayName("null stepName 应返回错误")
        void shouldFailForNullStepName() {
            ToolResult result = tool.execute(Map.of(), ctx);

            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("缺少必填参数");
        }
    }

    @Nested
    @DisplayName("成功创建")
    class SuccessfulCreation {

        @Test
        @DisplayName("有效参数应成功创建工步并返回 ID")
        void shouldCreateWorkStepSuccessfully() {
            // 模拟 MyBatis useGeneratedKeys：设置实体 ID
            when(workStepService.addWorkStep(any(WorkStep.class))).thenAnswer(inv -> {
                WorkStep s = inv.getArgument(0);
                s.setId(100);
                return 1;
            });

            ToolResult result = tool.execute(Map.of(
                    "stepName", " 装配-001 ",
                    "stepDescription", "精密装配工序"
            ), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKey("workStepId");
            assertThat(data).containsEntry("stepName", "装配-001");
            assertThat(data).containsEntry("message", "成功添加工步：装配-001");
        }

        @Test
        @DisplayName("stepName 应被 trim 处理")
        void shouldTrimStepName() {
            when(workStepService.addWorkStep(any(WorkStep.class))).thenAnswer(inv -> {
                WorkStep s = inv.getArgument(0);
                s.setId(101);
                return 1;
            });

            tool.execute(Map.of("stepName", "  带空格名称  "), ctx);

            ArgumentCaptor<WorkStep> captor = ArgumentCaptor.forClass(WorkStep.class);
            verify(workStepService).addWorkStep(captor.capture());
            assertEquals("带空格名称", captor.getValue().getName());
        }

        @Test
        @DisplayName("可选参数应正确赋值到 WorkStep")
        void shouldSetOptionalParams() {
            when(workStepService.addWorkStep(any(WorkStep.class))).thenAnswer(inv -> {
                WorkStep s = inv.getArgument(0);
                s.setId(102);
                return 1;
            });

            tool.execute(Map.of(
                    "stepName", "测试工步",
                    "stepDescription", "测试描述",
                    "functionDescription", "铣削功能",
                    "equipmentId", 42,
                    "functionId", 7
            ), ctx);

            ArgumentCaptor<WorkStep> captor = ArgumentCaptor.forClass(WorkStep.class);
            verify(workStepService).addWorkStep(captor.capture());
            WorkStep step = captor.getValue();
            assertEquals("测试工步", step.getName());
            assertEquals("测试描述", step.getDescription());
            assertEquals("铣削功能", step.getFunctionDescription());
            assertEquals(42, step.getEquipmentId());
            assertEquals(7, step.getFunctionId());
        }
    }

    @Nested
    @DisplayName("失败处理")
    class FailureHandling {

        @Test
        @DisplayName("数据库插入返回 0 时应返回错误")
        void shouldReturnErrorWhenInsertReturnsZero() {
            when(workStepService.addWorkStep(any(WorkStep.class))).thenReturn(0);

            ToolResult result = tool.execute(Map.of("stepName", "测试工步"), ctx);

            assertFalse(result.isSuccess());
            assertEquals("添加工步失败", result.getErrorMessage());
        }
    }
}
