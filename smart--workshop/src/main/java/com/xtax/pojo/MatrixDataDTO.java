package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
