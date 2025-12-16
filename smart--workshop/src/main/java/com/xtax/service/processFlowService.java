package com.xtax.service;

import com.xtax.pojo.FlowChartData;
import com.xtax.pojo.FlowChartDataDTO;
import com.xtax.pojo.ProcessFlow;

import java.util.List;

public interface processFlowService {
    //查询所有工艺流程信息
    List<ProcessFlow> getAllFlow();

    //查询指定工艺流程的流程图信息
    FlowChartData getProcessFlowChart(Integer id);

    //保存流程图信息
    void saveProcessFlowChart(FlowChartDataDTO flowChartDataDTO);

    //添加工艺流程信息
    void addProcessFlow(ProcessFlow processFlow);

    //修改工艺流程信息
    void updateProcessFlow(ProcessFlow processFlow);

    //删除工艺流程信息
    void deleteProcessFlow(Integer id);
}
