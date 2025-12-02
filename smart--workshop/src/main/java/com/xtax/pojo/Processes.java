package com.xtax.pojo;

import com.xtax.pojo.WorkStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Processes {
    private Integer id;
    private String processName;
    private Double plannedWorkingHours;
    private String description;
    private String qualityControlPoint;

    // 包含的BOM
    private List<Integer> inputBomId;   //输入的BOMid
    private List<Integer> outputBomId;  //输出的BOMid
    // 包含的工步
    private List<Integer> workStepId;
}
