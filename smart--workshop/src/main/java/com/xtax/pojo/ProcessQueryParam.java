package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
