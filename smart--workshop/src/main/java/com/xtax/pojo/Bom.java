package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bom {
    private Integer id;
    private String levelNum;
    private String drawingNo;
    private String nameSpecification;
    private String material;
    private Integer quantity;
    private Double unitWeight;
    private String type;
}
