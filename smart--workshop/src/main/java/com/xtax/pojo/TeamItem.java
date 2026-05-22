package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 团队项实体类
 * 团队的基本信息项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamItem {
    private String teamNo;
    private String teamName;
}
