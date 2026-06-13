package com.xtax.ai.tool.process;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.ProcessFlow;
import com.xtax.service.processFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索工序工具。
 * 按关键词搜索工艺流程/工序。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_process",
    description = "按名称或编号模糊搜索工艺流程/工序。返回匹配的工序列表。",
    category = "工序管理",
    label = "搜索工序"
)
@Component
@RequiredArgsConstructor
public class SearchProcessTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（工序名称或编号）", required = true)
    private String keyword;

    private final processFlowService processFlowService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        List<ProcessFlow> allProcesses = processFlowService.getAllFlow();
        if (allProcesses == null || allProcesses.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "processes", List.of()));
        }

        // 按关键词过滤
        List<ProcessFlow> filtered = allProcesses.stream()
                .filter(p -> {
                    String flowName = p.getFlowName() != null ? p.getFlowName() : "";
                    String idStr = p.getId() != null ? p.getId().toString() : "";
                    return flowName.contains(keyword) || idStr.contains(keyword);
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "processes", filtered
        ));
    }
}
