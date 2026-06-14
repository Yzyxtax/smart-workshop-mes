package com.xtax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工序实体类
 * 描述生产工序的详细信息，包括工序名称、计划工时、质检点等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Processes {
    private Integer id;
    private String processName;
    private Double plannedWorkingHours;
    private String description;
    private String qualityControlPoint;

    // 包含的BOM
    private List<Integer> inputBomId;   //输入的BOMid
    private List<Integer> outputBomId;  //输出的BOMid
    // 包含的工步
    private List<Integer> workStepId;
}
