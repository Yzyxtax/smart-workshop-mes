package com.xtax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工艺流程实体类
 * 描述工艺路线的基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessFlow {
    private Integer id;
    private Integer bomId;
    private String flowName;
    private String status;
    private Integer plannedWorkingHours;
}
