package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
