package com.xtax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生产线实体类
 * 描述生产线的基本信息，包括线号、名称、状态等
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionLine {
    private String lineNo;
    private String lineName;
    private String lineStatus;
    private Integer flowId;
    private String flowName;
}
