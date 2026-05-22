package com.xtax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作团队实体类
 * 描述团队的详细信息，包括团队编号、名称、位置、负责人等
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkTeam {
    private String teamNo;
    private String teamName;
    private String teamLocation;
    private String lineNo;
    private String lineName;
    private String teamLeader;
    private String leaderName;
    private Integer teamMemberNum;
    private List<String> userName;
}
