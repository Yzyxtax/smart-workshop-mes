package com.xtax.ai.tool.workstep;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.WorkStep;
import com.xtax.service.workStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索工步工具。
 * 按关键词搜索工步信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_work_step",
    description = "按名称或编号模糊搜索工步。返回匹配的工步列表。",
    category = "工步管理",
    label = "搜索工步"
)
@Component
@RequiredArgsConstructor
public class SearchWorkStepTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（工步名称或编号）", required = true)
    private String keyword;

    private final workStepService workStepService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        List<WorkStep> allSteps = workStepService.getAllWorkStep();
        if (allSteps == null || allSteps.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "workSteps", List.of()));
        }

        // 按关键词过滤
        List<WorkStep> filtered = allSteps.stream()
                .filter(s -> {
                    String name = s.getName() != null ? s.getName() : "";
                    String idStr = s.getId() != null ? s.getId().toString() : "";
                    return name.contains(keyword) || idStr.contains(keyword);
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "workSteps", filtered
        ));
    }
}
