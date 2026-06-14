package com.xtax.ai.tool.production;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.StateEnum;
import com.xtax.service.workOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * QueryWorkOrderStatusTool 单元测试。
 * 验证多条件 AND 过滤逻辑的正确性。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("QueryWorkOrderStatusTool 查询工单状态测试")
@ExtendWith(MockitoExtension.class)
class QueryWorkOrderStatusToolTest {

    @Mock
    private workOrderService workOrderService;

    @InjectMocks
    private QueryWorkOrderStatusTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    private WorkOrder buildWo(String no, StateEnum status, String orderNo,
                               Boolean isCritical) {
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(no);
        wo.setStatus(status);
        wo.setOrderNo(orderNo);
        wo.setIsCritical(isCritical);
        return wo;
    }

    @Nested
    @DisplayName("单条件过滤")
    class SingleFilter {

        @Test
        @DisplayName("按 workOrderNo 精确查询")
        void shouldMatchExactWorkOrderNo() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", true),
                    buildWo("WO-002", StateEnum.RUNNING, "ORD-A", false)
            ));

            ToolResult result = tool.execute(
                    Map.of("workOrderNo", "WO-001"), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }

        @Test
        @DisplayName("按 status 筛选")
        void shouldFilterByStatus() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false),
                    buildWo("WO-002", StateEnum.COMPLETED, "ORD-B", false)
            ));

            ToolResult result = tool.execute(Map.of("status", "RUNNING"), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }

        @Test
        @DisplayName("按 orderNo 筛选")
        void shouldFilterByOrderNo() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false),
                    buildWo("WO-002", StateEnum.RUNNING, "ORD-B", false)
            ));

            ToolResult result = tool.execute(Map.of("orderNo", "ORD-B"), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }

        @Test
        @DisplayName("按 isCritical 筛选")
        void shouldFilterByIsCritical() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", true),
                    buildWo("WO-002", StateEnum.RUNNING, "ORD-A", false),
                    buildWo("WO-003", StateEnum.RUNNING, "ORD-A", true)
            ));

            ToolResult result = tool.execute(Map.of("isCritical", true), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(2, data.get("count"));
        }
    }

    @Nested
    @DisplayName("多条件 AND 组合过滤")
    class MultiFilterAnd {

        @Test
        @DisplayName("status + orderNo 组合应 AND 逻辑")
        void shouldApplyAndLogicForStatusAndOrderNo() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false),
                    buildWo("WO-002", StateEnum.RUNNING, "ORD-B", false),
                    buildWo("WO-003", StateEnum.PAUSED, "ORD-A", false)
            ));

            ToolResult result = tool.execute(Map.of(
                    "status", "RUNNING",
                    "orderNo", "ORD-A"
            ), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"), "只有 WO-001 同时满足 RUNNING + ORD-A");
        }

        @Test
        @DisplayName("全条件组合：workOrderNo + status + orderNo + isCritical")
        void shouldApplyAllFiltersWithAndLogic() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", true),
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-B", true),
                    buildWo("WO-002", StateEnum.RUNNING, "ORD-A", true),
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false)
            ));

            ToolResult result = tool.execute(Map.of(
                    "workOrderNo", "WO-001",
                    "status", "RUNNING",
                    "orderNo", "ORD-A",
                    "isCritical", true
            ), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"), "只有第一条满足全部 4 个条件");
        }
    }

    @Nested
    @DisplayName("边界情况")
    class EdgeCases {

        @Test
        @DisplayName("空数据应安全返回")
        void shouldHandleEmptyData() {
            when(workOrderService.getAllWorkOrders()).thenReturn(Collections.emptyList());

            ToolResult result = tool.execute(Map.of("status", "RUNNING"), ctx);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("无参数时应返回全部数据")
        void shouldReturnAllWhenNoFilters() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false),
                    buildWo("WO-002", StateEnum.COMPLETED, "ORD-B", true)
            ));

            ToolResult result = tool.execute(Map.of(), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(2, data.get("count"));
        }

        @Test
        @DisplayName("空字符串参数应被忽略")
        void shouldIgnoreEmptyStringParams() {
            when(workOrderService.getAllWorkOrders()).thenReturn(List.of(
                    buildWo("WO-001", StateEnum.RUNNING, "ORD-A", false)
            ));

            ToolResult result = tool.execute(Map.of(
                    "workOrderNo", "",
                    "status", "  ",
                    "orderNo", "ORD-A"
            ), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(1, data.get("count"));
        }
    }
}
