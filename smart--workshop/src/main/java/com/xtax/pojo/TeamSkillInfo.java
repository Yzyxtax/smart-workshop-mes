package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamSkillInfo {
    private String teamNo;
    private String teamName;
    private HashMap<String, Integer> processMatrix;
}
