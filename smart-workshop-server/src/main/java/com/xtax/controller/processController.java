package com.xtax.controller;

import com.xtax.dto.ProcessQueryParam;
import com.xtax.entity.Processes;
import com.xtax.vo.Result;
import com.xtax.vo.ResultPage;
import com.xtax.service.serviceImpl.processServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/process")
public class processController {
    @Autowired
    private processServiceImpl processServiceImpl;

    @GetMapping
    public Result getProcess(ProcessQueryParam processQueryParam){
        log.info("条件分页查询工序信息:{}", processQueryParam);
        ResultPage<Processes> processes = processServiceImpl.getProcess(processQueryParam);
        if(processes != null) return Result.success(processes);
        return Result.error("查询失败");
    }

    @PostMapping
    public Result addProcess(@RequestBody Processes processes){
        log.info("添加工序信息:{}", processes);
        int processId = processServiceImpl.addProcess(processes);
        if(processId != -1) return Result.success(processId);
        return Result.error("添加失败");
    }

    @DeleteMapping
    public Result deleteProcess(@RequestParam List<Integer> ids){
        log.info("批量删除工序信息:{}", ids);
        if(processServiceImpl.deleteProcess(ids)) return Result.success("删除成功");
        return Result.error("删除失败");
    }

    @PutMapping
    public Result updateProcess(@RequestBody Processes processes){
        log.info("更新工序信息:{}", processes);
        if(processServiceImpl.updateProcess(processes)) return Result.success("更新成功");
        return Result.error("更新失败");
    }

    @GetMapping("/listAll")
    public Result listAll(){
        log.info("查询所有工序信息");
        List<Processes> processes = processServiceImpl.listAll();
        if(processes != null) return Result.success(processes);
        return Result.error("查询失败");
    }

    @GetMapping("/{processId}")
    public Result getProcessById(@PathVariable Integer processId){
        log.info("查询工序名称:{}", processId);
        String name = processServiceImpl.getProcessById(processId);
        if(name != null) return Result.success(name);
        return Result.error("查询失败");
    }
}
