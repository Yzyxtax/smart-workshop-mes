package com.xtax.mapper;

import com.xtax.entity.Processes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface processMapper {
    //条件分页查询工序信息
    List<Processes> getProcess(String processName, List<Integer> inputBom, List<Integer> outputBom);

    //添加工序信息
    int addProcess(Processes  processes);

    //添加输入物料信息
    int insertInputBom(Integer processId, List<Integer> inputBomId);

    //添加输出物料信息
    int insertOutputBom(Integer processId, List<Integer> outputBomId);

    //添加工步信息
    int insertWorkStep(Integer processId, List<Integer> workStepId);

    //删除工序信息
    int deleteProcess(List<Integer> ids);

    //删除输入物料信息
    void deleteInputBom(Integer processId);

    //删除输出物料信息
    void deleteOutputBom(Integer processId);

    //删除工步信息
    void deleteWorkStep(Integer processId);

    //修改工序信息
    int updateProcess(Processes processes);

    //根据id查询工序信息
    @Select("select process_name from processes where id = #{processId}")
    String getProcessById(Integer processId);
}
