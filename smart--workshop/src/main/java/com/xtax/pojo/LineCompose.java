package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineCompose {
    private String lineNo;              //产线编号
    private List<compose> composes;     //班组信息

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class compose {
        private String teamNo;          //班组编号
        private String teamName;        //班组名称
        private String type;            //班组类型（"assigned" 已分配, "unassigned" 未分配）
        private HashMap<String, Integer> processMatrix;     //工序矩阵:工序名称-处理人数
    }
}
