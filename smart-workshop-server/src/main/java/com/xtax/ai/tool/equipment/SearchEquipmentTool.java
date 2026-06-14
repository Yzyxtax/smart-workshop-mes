package com.xtax.ai.tool.equipment;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.dto.EquipmentQueryParam;
import com.xtax.entity.Equipment;
import com.xtax.service.equipmentService;
import com.xtax.vo.ResultPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 搜索设备工具。
 * 按名称、型号或类型模糊搜索设备。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_equipment",
    description = "按名称、型号或类型模糊搜索设备。返回匹配的设备列表（含基本信息和 ID）。",
    category = "设备管理",
    label = "搜索设备"
)
@Component
@RequiredArgsConstructor
public class SearchEquipmentTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（设备名称、型号或类型）", required = true)
    private String keyword;

    private final equipmentService equipmentService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        // 使用分页参数搜索
        EquipmentQueryParam queryParam = new EquipmentQueryParam();
        queryParam.setPage(1);
        queryParam.setPageSize(20);

        ResultPage<Equipment> result = equipmentService.getAllEquipment(queryParam);

        // 过滤匹配关键词的设备
        List<Equipment> filtered = result.getRows().stream()
                .filter(e -> {
                    String name = e.getName() != null ? e.getName() : "";
                    String model = e.getModel() != null ? e.getModel() : "";
                    String type = e.getType() != null ? e.getType() : "";
                    return name.contains(keyword) || model.contains(keyword) || type.contains(keyword);
                })
                .toList();

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "equipment", filtered
        ));
    }
}
