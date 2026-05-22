package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备查询参数类
 * 用于设备查询的条件参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentQueryParam {
    private Integer page=1;
    private Integer pageSize=10;
    private String name;
    private String type;
    private String model;
}
