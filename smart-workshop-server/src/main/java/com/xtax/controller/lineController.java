package com.xtax.controller;

import com.xtax.entity.LineCompose;
import com.xtax.vo.LineComposeVO;
import com.xtax.entity.ProductionLine;
import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.lineServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/line")
public class lineController {
    @Autowired
    private lineServiceImpl lineService;

    @GetMapping
    public Result getAllLine(){
        log.info("获取所有产线信息");
        List<ProductionLine> productionLines = lineService.getAllLine();
        return Result.success(productionLines);
    }

    @PostMapping
    public Result addLine(@RequestBody ProductionLine productionLine){
        log.info("添加产线信息");
        lineService.addLine(productionLine);
        return Result.success();
    }

    @PutMapping("/{lineCode}")
    public Result updateLine(@RequestBody ProductionLine productionLine, @PathVariable String lineCode){
        log.info("修改产线信息");
        lineService.updateLine(productionLine, lineCode);
        return Result.success();
    }

    @DeleteMapping("/{lineNo}")
    public Result deleteLine(@PathVariable String lineNo){
        log.info("删除产线信息");
        lineService.deleteLine(lineNo);
        return Result.success();
    }

    @GetMapping("/compose/{lineNo}")
    public Result getLineCompose(@PathVariable String lineNo){
        log.info("获取产线{}的班组信息",  lineNo);
        LineCompose lineCompose = lineService.getLineCompose(lineNo);
        return Result.success(lineCompose);
    }

    @PutMapping("/compose")
    public Result updateLineCompose(@RequestBody LineComposeVO lineCompose){
        log.info("保存产线{}班组信息",  lineCompose.getLineNo());
        lineService.updateLineCompose(lineCompose);
        return Result.success();
    }

    @GetMapping("/{lineNo}")
    public Result getLine(@PathVariable String lineNo){
        log.info("获取产线{}信息",  lineNo);
        ProductionLine productionLine = lineService.getLine(lineNo);
        return Result.success(productionLine);
    }
}
