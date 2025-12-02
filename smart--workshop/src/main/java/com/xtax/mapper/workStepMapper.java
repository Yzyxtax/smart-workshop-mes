package com.xtax.mapper;

import com.xtax.pojo.WorkStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface workStepMapper {
    //查询工步信息
    List<WorkStep> getWorkStep(String stepName, String equipmentName, String processName);

    //根据工步id查询工序信息
    List<String> getProcessNamesByStepId(Long stepId);

    //添加工步信息
    int addWorkStep(WorkStep workStep);

    //修改工步信息
    int updateWorkStep(WorkStep workStep);

    //删除工步信息
    int deleteWorkStep(List<Integer> stepIds);

    //查询所有工步信息
    List<WorkStep> getAllWorkStep();
}
