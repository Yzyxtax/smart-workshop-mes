package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowChartData {
    private Integer flowId;
    private List<String> nodeData;
    private HashMap<String,List<String>> edgeData;
}
