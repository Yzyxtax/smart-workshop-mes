package com.xtax.ai.tool.production;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.ProductionOrder;
import com.xtax.service.orderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询订单状态工具。
 * 查询生产订单的状态与基本信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "query_order_status",
    description = "查询生产订单的状态与基本信息。可按订单编号精确查询或按状态筛选。",
    category = "生产管理",
    label = "查询订单状态"
)
@Component
@RequiredArgsConstructor
public class QueryOrderStatusTool implements ToolHandler {

    @ToolParam(description = "订单编号（精确匹配）")
    private String orderNo;

    @ToolParam(description = "状态筛选",
        enumValues = {"CREATED", "RELEASED", "RUNNING", "PAUSED", "COMPLETED", "TERMINATED"})
    private String status;

    @ToolParam(description = "所属计划编号")
    private String planNo;

    private final orderService orderService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String orderNo = (String) params.get("orderNo");
        String status = (String) params.get("status");
        String planNo = (String) params.get("planNo");

        List<ProductionOrder> allOrders = orderService.getAllOrder();
        if (allOrders == null || allOrders.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "orders", List.of()));
        }

        // 按条件过滤
        List<ProductionOrder> filtered = allOrders.stream()
                .filter(o -> {
                    boolean match = true;
                    if (orderNo != null && !orderNo.trim().isEmpty()) {
                        String no = o.getOrderNo() != null ? o.getOrderNo() : "";
                        match = no.equals(orderNo.trim());
                    }
                    if (status != null && !status.trim().isEmpty()) {
                        match = match && (o.getStatus() != null
                                && o.getStatus().name().equalsIgnoreCase(status.trim()));
                    }
                    if (planNo != null && !planNo.trim().isEmpty()) {
                        String pn = o.getPlanNo() != null ? o.getPlanNo() : "";
                        match = match && pn.equals(planNo.trim());
                    }
                    return match;
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "orders", filtered
        ));
    }
}
