package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工序步骤查询参数类
 * 用于工序步骤查询的条件参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkStepQueryParam {
    private Integer page=1;
    private Integer pageSize=10;
    private String stepName;
    private String equipmentName;
    private String processName;

}
