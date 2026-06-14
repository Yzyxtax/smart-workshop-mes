package com.xtax.ai.tool.equipment;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.Equipment;
import com.xtax.service.equipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 获取设备详情工具。
 * 根据设备 ID 获取设备的完整信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "get_equipment_detail",
    description = "根据设备 ID 获取设备的详细信息，包含所有功能列表。",
    category = "设备管理",
    label = "获取设备详情"
)
@Component
@RequiredArgsConstructor
public class GetEquipmentDetailTool implements ToolHandler {

    @ToolParam(description = "设备 ID", required = true)
    private Integer equipmentId;

    private final equipmentService equipmentService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        Object idObj = params.get("equipmentId");
        if (idObj == null) {
            return ToolResult.error("缺少必填参数【equipmentId】");
        }
        Integer equipmentId = Integer.valueOf(idObj.toString());
        Equipment equipment = equipmentService.getEquipmentById(equipmentId);
        if (equipment == null) {
            return ToolResult.error("设备不存在，ID: " + equipmentId);
        }
        return ToolResult.success(equipment);
    }
}
