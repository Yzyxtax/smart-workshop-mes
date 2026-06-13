package com.xtax.ai.tool.production;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.WorkOrder;
import com.xtax.service.workOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询工单状态工具。
 * 查询生产工单的状态与基本信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "query_work_order_status",
    description = "查询生产工单的状态与基本信息。可按工单编号精确查询或按状态筛选。",
    category = "生产管理",
    label = "查询工单状态"
)
@Component
@RequiredArgsConstructor
public class QueryWorkOrderStatusTool implements ToolHandler {

    @ToolParam(description = "工单编号（精确匹配）")
    private String workOrderNo;

    @ToolParam(description = "状态筛选",
        enumValues = {"CREATED", "RELEASED", "RUNNING", "PAUSED", "COMPLETED", "TERMINATED"})
    private String status;

    @ToolParam(description = "所属订单编号")
    private String orderNo;

    @ToolParam(description = "是否只查询关键工单")
    private Boolean isCritical;

    private final workOrderService workOrderService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String workOrderNo = (String) params.get("workOrderNo");
        String status = (String) params.get("status");
        String orderNo = (String) params.get("orderNo");
        Object isCriticalObj = params.get("isCritical");

        List<WorkOrder> allWorkOrders = workOrderService.getAllWorkOrders();
        if (allWorkOrders == null || allWorkOrders.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "workOrders", List.of()));
        }

        // 按条件过滤
        List<WorkOrder> filtered = allWorkOrders.stream()
                .filter(w -> {
                    boolean match = true;
                    if (workOrderNo != null && !workOrderNo.trim().isEmpty()) {
                        String no = w.getWorkOrderNo() != null ? w.getWorkOrderNo() : "";
                        match = no.equals(workOrderNo.trim());
                    }
                    if (status != null && !status.trim().isEmpty()) {
                        match = match && (w.getStatus() != null
                                && w.getStatus().name().equalsIgnoreCase(status.trim()));
                    }
                    if (orderNo != null && !orderNo.trim().isEmpty()) {
                        String on = w.getOrderNo() != null ? w.getOrderNo() : "";
                        match = match && on.equals(orderNo.trim());
                    }
                    if (isCriticalObj != null) {
                        boolean isCritical = Boolean.parseBoolean(isCriticalObj.toString());
                        match = match && (w.getIsCritical() != null
                                && w.getIsCritical() == isCritical);
                    }
                    return match;
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "workOrders", filtered
        ));
    }
}
