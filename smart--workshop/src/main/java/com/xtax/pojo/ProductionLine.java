package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
