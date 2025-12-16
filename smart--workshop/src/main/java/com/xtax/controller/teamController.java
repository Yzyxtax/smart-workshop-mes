package com.xtax.controller;

import com.xtax.pojo.*;
import com.xtax.service.serviceImpl.teamServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/team")
public class teamController {
    @Autowired
    private teamServiceImpl teamServiceImpl;

    @GetMapping
    public Result getAllTeam(){
        log.info("查询所有班组信息");
        List<TeamItem> teamList = teamServiceImpl.getAllTeam();
        return Result.success(teamList);
    }

    @GetMapping("/{teamCode}")
    public Result getTeamByNo(@PathVariable String teamCode){
        log.info("查询编号为{}的班组信息", teamCode);
        WorkTeam team = teamServiceImpl.getTeamByNo(teamCode);
        return Result.success(team);
    }

    @PostMapping
    public Result addTeam(@RequestBody WorkTeam team){
        log.info("添加班组信息：{}", team);
        teamServiceImpl.addTeam(team);
        return Result.success();
    }

    @PutMapping("/{teamCode}")
    public Result updateTeam(@RequestBody WorkTeam team, @PathVariable String teamCode){
        log.info("修改班组信息：{}", team);
        teamServiceImpl.updateTeam(team, teamCode);
        return Result.success();
    }

    @DeleteMapping("/{teamCode}")
    public Result deleteTeam(@PathVariable String teamCode){
        log.info("删除班组信息：{}", teamCode);
        teamServiceImpl.deleteTeam(teamCode);
        return Result.success();
    }

    @GetMapping("/matrix")
    public Result getMatrixData(String teamCode){
        log.info("查询技能矩阵信息");
        List<MatrixDataVO> matrixData = teamServiceImpl.getMatrixData(teamCode);
        return Result.success(matrixData);
    }

    @PostMapping("/matrix")
    public Result saveMatrixData(@RequestBody MatrixDataDTO matrixDataDTO){
        log.info("保存技能矩阵信息：{}", matrixDataDTO);
        teamServiceImpl.saveMatrixData(matrixDataDTO);
        return Result.success();
    }
}
