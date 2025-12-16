package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowChartDataDTO {
    private Integer flowId;
    private List<String> nodeData;
    private List<EdgeData> edgeData;
}
