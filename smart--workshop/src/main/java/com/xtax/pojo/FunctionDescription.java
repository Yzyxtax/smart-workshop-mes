package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述实体类
 * 描述设备或工序的功能信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionDescription {
    private Integer id;
    private String functionDescription;
}
