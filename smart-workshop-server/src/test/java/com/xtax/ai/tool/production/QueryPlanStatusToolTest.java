package com.xtax.ai.tool.production;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.Plan;
import com.xtax.enums.StateEnum;
import com.xtax.service.planService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * QueryPlanStatusTool 单元测试。
 * 验证计划编号精确匹配和状态筛选逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("QueryPlanStatusTool 查询计划状态测试")
@ExtendWith(MockitoExtension.class)
class QueryPlanStatusToolTest {

    @Mock
    private planService planService;

    @InjectMocks
    private QueryPlanStatusTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    private Plan buildPlan(String planNo, StateEnum status) {
        Plan p = new Plan();
        p.setPlanNo(planNo);
        p.setStatus(status);
        return p;
    }

    @Test
    @DisplayName("按 planNo 精确查询应只返回匹配项")
    void shouldExactMatchPlanNo() {
        when(planService.getAllPlan()).thenReturn(List.of(
                buildPlan("PLAN-001", StateEnum.RUNNING),
                buildPlan("PLAN-002", StateEnum.RUNNING),
                buildPlan("PLAN-001A", StateEnum.RUNNING)  // 不应匹配
        ));

        ToolResult result = tool.execute(Map.of("planNo", "PLAN-001"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1, data.get("count"), "精确匹配，PLAN-001A 不应返回");
    }

    @Nested
    @DisplayName("状态筛选大小写不敏感")
    class CaseInsensitiveStatus {

        @Test
        @DisplayName("小写 running 应匹配 RUNNING 状态")
        void shouldMatchLowercaseStatus() {
            when(planService.getAllPlan()).thenReturn(List.of(
                    buildPlan("PLAN-001", StateEnum.RUNNING),
                    buildPlan("PLAN-002", StateEnum.COMPLETED)
            ));

            ToolResult result = tool.execute(Map.of("status", "running"), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }

        @Test
        @DisplayName("混合大小写 Running 应匹配 RUNNING 状态")
        void shouldMatchMixedCaseStatus() {
            when(planService.getAllPlan()).thenReturn(List.of(
                    buildPlan("PLAN-001", StateEnum.RUNNING)
            ));

            ToolResult result = tool.execute(Map.of("status", "Running"), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }
    }

    @Test
    @DisplayName("planNo + status AND 组合过滤")
    void shouldApplyAndLogic() {
        when(planService.getAllPlan()).thenReturn(List.of(
                buildPlan("PLAN-001", StateEnum.RUNNING),
                buildPlan("PLAN-002", StateEnum.RUNNING),
                buildPlan("PLAN-001", StateEnum.COMPLETED)
        ));

        ToolResult result = tool.execute(Map.of(
                "planNo", "PLAN-001",
                "status", "RUNNING"
        ), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1, data.get("count"));
    }

    @Test
    @DisplayName("无参数时应返回全部数据")
    void shouldReturnAllWhenNoFilters() {
        when(planService.getAllPlan()).thenReturn(List.of(
                buildPlan("PLAN-001", StateEnum.RUNNING),
                buildPlan("PLAN-002", StateEnum.COMPLETED)
        ));

        ToolResult result = tool.execute(Map.of(), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2, data.get("count"));
    }
}
