package com.xtax.ai.tool.equipment;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.Equipment;
import com.xtax.entity.FunctionDescription;
import com.xtax.service.equipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 更新设备功能工具。
 * 修改指定设备的某条功能描述。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "update_equipment_function",
    description = "修改指定设备的某条功能描述。需提供设备 ID、功能 ID 和新的功能描述文本。",
    category = "设备管理",
    permissions = {"SYS_EQUIPMENT_MANAGE"},
    label = "更新设备功能"
)
@Component
@RequiredArgsConstructor
public class UpdateEquipmentFunctionTool implements ToolHandler {

    @ToolParam(description = "目标设备 ID", required = true)
    private Integer equipmentId;

    @ToolParam(description = "功能描述 ID", required = true)
    private Integer functionId;

    @ToolParam(description = "新的功能描述文本", required = true)
    private String newDescription;

    private final equipmentService equipmentService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        Object eqIdObj = params.get("equipmentId");
        Object funcIdObj = params.get("functionId");
        String newDesc = (String) params.get("newDescription");

        if (eqIdObj == null || funcIdObj == null || newDesc == null || newDesc.trim().isEmpty()) {
            return ToolResult.error("缺少必填参数：设备 ID、功能 ID 和新描述");
        }

        Integer eqId = Integer.valueOf(eqIdObj.toString());
        Integer funcId = Integer.valueOf(funcIdObj.toString());

        Equipment equipment = equipmentService.getEquipmentById(eqId);
        if (equipment == null) {
            return ToolResult.error("设备不存在，ID: " + eqId);
        }

        // 查找并更新功能描述
        if (equipment.getDescription() != null) {
            for (FunctionDescription func : equipment.getDescription()) {
                if (func.getId() != null && func.getId().equals(funcId)) {
                    func.setFunctionDescription(newDesc.trim());
                    int rows = equipmentService.updateEquipment(equipment);
                    if (rows > 0) {
                        return ToolResult.success(Map.of(
                                "equipmentId", eqId,
                                "functionId", funcId,
                                "newDescription", newDesc.trim(),
                                "message", "成功更新设备功能描述"
                        ));
                    }
                    return ToolResult.error("更新设备功能失败");
                }
            }
        }
        return ToolResult.error("功能描述不存在，ID: " + funcId);
    }
}
