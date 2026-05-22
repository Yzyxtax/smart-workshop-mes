package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

/**
 * 流程图数据传输对象
 * 用于传输流程图的节点和边数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowChartDataDTO {
    private Integer flowId;
    private List<String> nodeData;
    private List<EdgeData> edgeData;
}
