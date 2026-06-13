package com.xtax.ai.tool.bom;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.Bom;
import com.xtax.service.bomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 创建 BOM 工具。
 * 创建一条新的物料清单记录。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "create_bom",
    description = "创建一条新的 BOM（物料清单）记录。需要提供图号、名称规格、材质、单位用量和类型。",
    category = "BOM管理",
    permissions = {"SYS_BOM_MANAGE"},
    label = "创建 BOM"
)
@Component
@RequiredArgsConstructor
public class CreateBomTool implements ToolHandler {

    @ToolParam(description = "BOM 图号", required = true)
    private String bomDrawingNo;

    @ToolParam(description = "名称规格", required = true)
    private String bomNameSpec;

    @ToolParam(description = "材质")
    private String bomMaterial;

    @ToolParam(description = "单位用量")
    private Integer bomQuantity;

    @ToolParam(description = "BOM 类型", enumValues = {"成品", "半成品", "原材料"})
    private String bomType;

    private final bomService bomService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String drawingNo = (String) params.get("bomDrawingNo");
        String nameSpec = (String) params.get("bomNameSpec");

        if (drawingNo == null || drawingNo.trim().isEmpty()) {
            return ToolResult.error("缺少必填参数【bomDrawingNo】：BOM 图号");
        }
        if (nameSpec == null || nameSpec.trim().isEmpty()) {
            return ToolResult.error("缺少必填参数【bomNameSpec】：名称规格");
        }

        Bom bom = new Bom();
        bom.setDrawingNo(drawingNo.trim());
        bom.setNameSpecification(nameSpec.trim());
        bom.setMaterial((String) params.getOrDefault("bomMaterial", ""));
        bom.setQuantity(params.get("bomQuantity") != null
                ? Integer.valueOf(params.get("bomQuantity").toString()) : 1);
        bom.setType((String) params.getOrDefault("bomType", "原材料"));

        int rows = bomService.addBom(bom);
        if (rows > 0) {
            return ToolResult.success(Map.of(
                    "bomId", bom.getId(),
                    "drawingNo", bom.getDrawingNo(),
                    "nameSpec", bom.getNameSpecification(),
                    "type", bom.getType(),
                    "message", "成功创建 BOM：" + bom.getNameSpecification()
            ));
        }
        return ToolResult.error("创建 BOM 失败");
    }
}
