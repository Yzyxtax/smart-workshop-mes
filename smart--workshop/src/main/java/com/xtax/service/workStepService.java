package com.xtax.service;

import com.xtax.pojo.ResultPage;
import com.xtax.pojo.WorkStep;
import com.xtax.pojo.WorkStepQueryParam;

import java.util.List;

public interface workStepService {
    //查询工步信息
    public ResultPage<WorkStep> getWorkStep(WorkStepQueryParam workStepQueryParam);

    //添加工步信息
    int addWorkStep(WorkStep workStep);

    //修改工步信息
    int updateWorkStep(WorkStep workStep);

    //删除工步信息
    int deleteWorkStep(List<Integer> stepIds);

    //查询所有工步信息
    List<WorkStep> getAllWorkStep();
}
