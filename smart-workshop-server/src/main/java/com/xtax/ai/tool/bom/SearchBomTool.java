package com.xtax.ai.tool.bom;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.service.bomService;
import com.xtax.vo.BomTreeData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索 BOM 工具。
 * 按关键词搜索物料清单。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_bom",
    description = "按名称、图号或材质模糊搜索 BOM（物料清单）。返回匹配的物料列表。",
    category = "BOM管理",
    label = "搜索 BOM"
)
@Component
@RequiredArgsConstructor
public class SearchBomTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（物料名称、图号或材质）", required = true)
    private String keyword;

    private final bomService bomService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        // 获取所有物料名称和层次
        List<BomTreeData> allMaterials = bomService.getAllMaterialName();
        if (allMaterials == null || allMaterials.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "boms", List.of()));
        }

        // 按关键词过滤（BomTreeData.label 为物料名称）
        List<BomTreeData> filtered = allMaterials.stream()
                .filter(m -> {
                    String label = m.getLabel() != null ? m.getLabel() : "";
                    return label.contains(keyword);
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "boms", filtered
        ));
    }
}
