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

import java.util.Map;

/**
 * 添加工步工具。
 * 为指定设备添加一个新的工步。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "add_work_step",
    description = "为指定设备添加一个新的工步。需提供工步名称、描述、设备 ID 和功能 ID。",
    category = "工步管理",
    permissions = {"SYS_PROCESS_MANAGE"},
    label = "添加工步"
)
@Component
@RequiredArgsConstructor
public class AddWorkStepTool implements ToolHandler {

    @ToolParam(description = "工步名称", required = true)
    private String stepName;

    @ToolParam(description = "工步描述")
    private String stepDescription;

    @ToolParam(description = "关联设备 ID")
    private Integer equipmentId;

    @ToolParam(description = "关联功能 ID")
    private Integer functionId;

    private final workStepService workStepService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String stepName = (String) params.get("stepName");

        if (stepName == null || stepName.trim().isEmpty()) {
            return ToolResult.error("缺少必填参数：工步名称");
        }

        WorkStep step = new WorkStep();
        step.setName(stepName.trim());
        step.setDescription((String) params.getOrDefault("stepDescription", ""));
        if (params.get("functionDescription") != null) {
            step.setFunctionDescription((String) params.get("functionDescription"));
        }
        if (params.get("equipmentId") != null) {
            step.setEquipmentId(Integer.valueOf(params.get("equipmentId").toString()));
        }
        if (params.get("functionId") != null) {
            step.setFunctionId(Integer.valueOf(params.get("functionId").toString()));
        }

        int rows = workStepService.addWorkStep(step);
        if (rows > 0) {
            return ToolResult.success(Map.of(
                    "workStepId", step.getId(),
                    "stepName", step.getName(),
                    "message", "成功添加工步：" + step.getName()
            ));
        }
        return ToolResult.error("添加工步失败");
    }
}
