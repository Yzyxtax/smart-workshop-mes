package com.xtax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 物料清单（Bill of Materials）实体类
 * 描述产品的组成物料信息
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bom {
    private Integer id;
    private Integer parentId;
    private String drawingNo;
    private String nameSpecification;
    private String material;
    private Integer quantity;
    private Double unitWeight;
    private String type;
}
