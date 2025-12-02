package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
