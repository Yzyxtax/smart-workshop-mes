package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工序查询参数类
 * 用于工序查询的条件参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessQueryParam {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String processName;
    private List<Integer> inputBom;
    private List<Integer> outputBom;
}
