package com.xtax.service;

import com.xtax.pojo.Plan;

import java.util.List;

public interface planService {
    //获取所有计划信息
    List<Plan> getAllPlan();

    //添加计划信息
    int addPlan(Plan plan);

    //删除计划信息
    int deletePlan(String planNo);

    //更新计划信息
    int updatePlan(Plan plan, String planNo);
}
