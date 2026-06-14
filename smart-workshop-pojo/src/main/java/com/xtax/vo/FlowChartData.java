package com.xtax.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

/**
 * 流程图数据实体类
 * 描述流程图的完整数据结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowChartData {
    private Integer flowId;
    private List<String> nodeData;
    private HashMap<String,List<String>> edgeData;
}
