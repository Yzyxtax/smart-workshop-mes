package com.xtax.controller;

import com.xtax.pojo.FlowChartData;
import com.xtax.pojo.FlowChartDataDTO;
import com.xtax.pojo.ProcessFlow;
import com.xtax.pojo.Result;
import com.xtax.service.serviceImpl.processFlowServiceImpl;
import com.xtax.service.serviceImpl.processServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/flow")
public class processFlowController {
    @Autowired
    private processFlowServiceImpl processFlowServiceImpl;
    private Integer id;

    @GetMapping
    public Result getProcessFlow(){
        log.info("查询所有工序信息");
        List<ProcessFlow> flowList = processFlowServiceImpl.getAllFlow();
        return Result.success(flowList);
    }

    @GetMapping("/chart/{id}")
    public Result getProcessFlowChart(@PathVariable Integer id){
        log.info("查询工艺流程{}的流程图信息",id);
        FlowChartData flowChartData = processFlowServiceImpl.getProcessFlowChart(id);
        return Result.success(flowChartData);
    }

    @PostMapping("/chart")
    public Result saveProcessFlowChart(@RequestBody FlowChartDataDTO flowChartDataDTO){
        log.info("保存流程图信息");
        processFlowServiceImpl.saveProcessFlowChart(flowChartDataDTO);
        return Result.success();
    }

    @PostMapping
    public Result addProcessFlow(@RequestBody ProcessFlow processFlow){
        log.info("添加工艺流程信息");
        processFlowServiceImpl.addProcessFlow(processFlow);
        return Result.success();
    }

    @PutMapping
    public Result updateProcessFlow(@RequestBody ProcessFlow processFlow){
        log.info("修改工艺流程信息");
        processFlowServiceImpl.updateProcessFlow(processFlow);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteProcessFlow(Integer id){
        log.info("删除工艺流程信息");
        processFlowServiceImpl.deleteProcessFlow(id);
        return Result.success();
    }
}
