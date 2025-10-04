package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentQueryParam {
    private Integer page;
    private Integer pageSize;
    private String name;
    private String type;
    private String model;
}
