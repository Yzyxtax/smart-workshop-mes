package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
