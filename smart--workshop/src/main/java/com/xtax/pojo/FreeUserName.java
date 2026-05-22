package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 可用用户名实体类
 * 包含员工列表和负责人列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreeUserName {
    private List<String> empList;
    private List<String> LeaderList;
}
