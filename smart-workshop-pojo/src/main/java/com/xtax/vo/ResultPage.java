package com.xtax.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * 用于封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultPage<T> {
    private Long total;
    private List<T> rows;
}
