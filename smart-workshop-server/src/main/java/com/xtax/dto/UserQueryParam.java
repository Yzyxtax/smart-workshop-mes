package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 用户查询参数类
 * 用于用户查询的条件参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryParam {
    private Integer page=1;
    private Integer pageSize=10;
    private String name;
    private String position;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
}
