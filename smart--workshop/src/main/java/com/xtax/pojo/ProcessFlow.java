package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessFlow {
    private Integer id;
    private Integer bomId;
    private String flowName;
    private String status;
    private Integer plannedWorkingHours;
}
