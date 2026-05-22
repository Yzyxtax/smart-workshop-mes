package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产线组成视图对象
 * 用于展示产线与班组的关联关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineComposeVO {
    private String lineNo;
    private List<String> teams;
}
