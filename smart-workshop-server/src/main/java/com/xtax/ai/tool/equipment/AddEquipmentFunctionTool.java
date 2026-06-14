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

import java.util.ArrayList;
import java.util.Map;

/**
 * 添加设备功能工具。
 * 为指定设备新增功能描述。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "add_equipment_function",
    description = "为指定的设备新增一条功能描述。需提供设备 ID 和功能描述文本。",
    category = "设备管理",
    permissions = {"SYS_EQUIPMENT_MANAGE"},
    label = "添加设备功能"
)
@Component
@RequiredArgsConstructor
public class AddEquipmentFunctionTool implements ToolHandler {

    @ToolParam(description = "目标设备 ID", required = true)
    private Integer equipmentId;

    @ToolParam(description = "功能描述文本", required = true)
    private String functionDescription;

    private final equipmentService equipmentService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        Object idObj = params.get("equipmentId");
        String desc = (String) params.get("functionDescription");

        if (idObj == null || desc == null || desc.trim().isEmpty()) {
            return ToolResult.error("缺少必填参数：设备 ID 和功能描述");
        }

        Integer eqId = Integer.valueOf(idObj.toString());
        Equipment equipment = equipmentService.getEquipmentById(eqId);
        if (equipment == null) {
            return ToolResult.error("设备不存在，ID: " + eqId);
        }

        // 添加功能描述到设备的 description 列表
        FunctionDescription func = new FunctionDescription();
        func.setFunctionDescription(desc.trim());

        if (equipment.getDescription() == null) {
            equipment.setDescription(new ArrayList<>());
        }
        equipment.getDescription().add(func);

        int rows = equipmentService.updateEquipment(equipment);
        if (rows > 0) {
            return ToolResult.success(Map.of(
                    "equipmentId", eqId,
                    "functionDescription", desc.trim(),
                    "message", "成功为设备【" + equipment.getName() + "】添加功能：" + desc.trim()
            ));
        }
        return ToolResult.error("添加设备功能失败");
    }
}
