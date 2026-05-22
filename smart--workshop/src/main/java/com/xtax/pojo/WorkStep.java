package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 工序步骤实体类
 * 描述生产工序的具体步骤信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkStep {
    private Integer id;
    private String name;
    private Integer equipmentId;
    private Integer functionId;
    private String equipmentName;
    private String equipmentModel;
    private String functionDescription;
    private String description;
    private List<String> processName;
}
