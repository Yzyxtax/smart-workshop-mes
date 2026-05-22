package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 团队技能信息实体类
 * 描述团队的技能矩阵信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamSkillInfo {
    private String teamNo;
    private String teamName;
    private HashMap<String, Integer> processMatrix;
}
