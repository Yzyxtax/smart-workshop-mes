package com.xtax.controller;

import com.xtax.vo.Result;
import com.xtax.vo.ResultPage;
import com.xtax.entity.WorkStep;
import com.xtax.dto.WorkStepQueryParam;
import com.xtax.service.serviceImpl.workStepServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/step")
public class workStepController {
    @Autowired
    private workStepServiceImpl workStepServiceImpl;

    @GetMapping
    public Result getAllWorkStep(WorkStepQueryParam workStepQueryParam){
        log.info("查询工步信息:{}", workStepQueryParam);
        ResultPage<WorkStep> workSteps = workStepServiceImpl.getWorkStep(workStepQueryParam);
        if(workSteps != null) return Result.success(workSteps);
        return Result.error("查询失败");
    }

    @PostMapping
    public Result addWorkStep(@RequestBody WorkStep workStep){
        log.info("添加工步：{}", workStep);
        int add = workStepServiceImpl.addWorkStep(workStep);
        if(add > 0) return Result.success("添加成功");
        return Result.error("添加失败");
    }

    @PutMapping
    public Result updateWorkStep(@RequestBody WorkStep workStep){
        log.info("更新工步：{}", workStep);
        int update = workStepServiceImpl.updateWorkStep(workStep);
        if(update > 0) return Result.success("更新成功");
        return Result.error("更新失败");
    }

    @DeleteMapping
    public Result deleteWorkStep(@RequestParam List<Integer> ids){
        log.info("删除工步：{}", ids);
        int delete = workStepServiceImpl.deleteWorkStep(ids);
        if(delete > 0) return Result.success("删除成功");
        return Result.error("删除失败");
    }

    @GetMapping("/listAll")
    public Result getAllWorkStep(){
        log.info("查询所有工步");
        List<WorkStep> list = workStepServiceImpl.getAllWorkStep();
        if(list != null) return Result.success(list);
        return Result.error("查询失败");
    }
}
