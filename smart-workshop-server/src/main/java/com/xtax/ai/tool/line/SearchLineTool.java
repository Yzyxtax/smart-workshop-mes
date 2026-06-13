package com.xtax.ai.tool.line;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.ProductionLine;
import com.xtax.service.lineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索产线工具。
 * 按关键词搜索产线信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_line",
    description = "按名称或编号模糊搜索产线。返回匹配的产线列表（含基本信息和状态）。",
    category = "产线管理",
    label = "搜索产线"
)
@Component
@RequiredArgsConstructor
public class SearchLineTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（产线名称或编号）", required = true)
    private String keyword;

    private final lineService lineService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        List<ProductionLine> allLines = lineService.getAllLine();
        if (allLines == null || allLines.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "lines", List.of()));
        }

        // 按关键词过滤
        List<ProductionLine> filtered = allLines.stream()
                .filter(l -> {
                    String name = l.getLineName() != null ? l.getLineName() : "";
                    String no = l.getLineNo() != null ? l.getLineNo() : "";
                    return name.contains(keyword) || no.contains(keyword);
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "lines", filtered
        ));
    }
}
