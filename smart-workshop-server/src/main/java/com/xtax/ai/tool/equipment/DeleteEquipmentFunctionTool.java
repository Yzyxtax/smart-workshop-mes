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
 * 删除设备功能工具（破坏性操作，需二次确认）。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "delete_equipment_function",
    description = "删除指定设备的某条功能描述。此操作不可撤销，必须获得用户明确确认后才能执行。",
    category = "设备管理",
    permissions = {"SYS_EQUIPMENT_MANAGE"},
    requiresConfirmation = true,
    label = "删除设备功能"
)
@Component
@RequiredArgsConstructor
public class DeleteEquipmentFunctionTool implements ToolHandler {

    @ToolParam(description = "目标设备 ID", required = true)
    private Integer equipmentId;

    @ToolParam(description = "要删除的功能描述 ID", required = true)
    private Integer functionId;

    private final equipmentService equipmentService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        Object eqIdObj = params.get("equipmentId");
        Object funcIdObj = params.get("functionId");

        if (eqIdObj == null || funcIdObj == null) {
            return ToolResult.error("缺少必填参数：设备 ID 和功能 ID");
        }

        Integer eqId = Integer.valueOf(eqIdObj.toString());
        Integer funcId = Integer.valueOf(funcIdObj.toString());

        Equipment equipment = equipmentService.getEquipmentById(eqId);
        if (equipment == null) {
            return ToolResult.error("设备不存在，ID: " + eqId);
        }

        // 查找并删除功能描述
        if (equipment.getDescription() != null) {
            boolean removed = equipment.getDescription().removeIf(
                    func -> func.getId() != null && func.getId().equals(funcId));
            if (removed) {
                equipmentService.updateEquipment(equipment);
                return ToolResult.success(Map.of(
                        "equipmentId", eqId,
                        "functionId", funcId,
                        "message", "成功删除设备功能"
                ));
            }
        }
        return ToolResult.error("功能描述不存在，ID: " + funcId);
    }
}
