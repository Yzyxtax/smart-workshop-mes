package com.xtax.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 矩阵数据视图对象
 * 用于展示用户的工序选择信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatrixDataVO {
    private String username;
    private String name;
    List<ProcessChoice> choices;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProcessChoice {
        private String processName;
        private Boolean choose;
    }
}
