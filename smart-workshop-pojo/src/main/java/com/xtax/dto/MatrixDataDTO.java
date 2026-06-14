package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 矩阵数据传输对象
 * 用于传输团队成员的工序矩阵数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatrixDataDTO {
    private String teamCode;
    private List<MatrixData> matrixData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatrixData {
        private String username;
        private String processName;
    }
}
