package com.xtax.ai.tool.production;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.Plan;
import com.xtax.service.planService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询计划状态工具。
 * 查询生产计划的状态与基本信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "query_plan_status",
    description = "查询生产计划的状态与基本信息。可按计划编号精确查询或按状态筛选。",
    category = "生产管理",
    label = "查询计划状态"
)
@Component
@RequiredArgsConstructor
public class QueryPlanStatusTool implements ToolHandler {

    @ToolParam(description = "计划编号（精确匹配）")
    private String planNo;

    @ToolParam(description = "状态筛选",
        enumValues = {"CREATED", "RELEASED", "RUNNING", "PAUSED", "COMPLETED", "TERMINATED"})
    private String status;

    private final planService planService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String planNo = (String) params.get("planNo");
        String status = (String) params.get("status");

        List<Plan> allPlans = planService.getAllPlan();
        if (allPlans == null || allPlans.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "plans", List.of()));
        }

        // 按条件过滤
        List<Plan> filtered = allPlans.stream()
                .filter(p -> {
                    boolean match = true;
                    if (planNo != null && !planNo.trim().isEmpty()) {
                        String no = p.getPlanNo() != null ? p.getPlanNo() : "";
                        match = no.equals(planNo.trim());
                    }
                    if (status != null && !status.trim().isEmpty()) {
                        match = match && (p.getStatus() != null
                                && p.getStatus().name().equalsIgnoreCase(status.trim()));
                    }
                    return match;
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "plans", filtered
        ));
    }
}
