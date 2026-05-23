package com.xtax.controller;

import com.xtax.exception.BusinessException;
import com.xtax.entity.Plan;
import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.planServiceImpl;
import com.xtax.service.serviceImpl.planStateServiceImpl;
import com.xtax.enums.ActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/plan")
public class planController {
    @Autowired
    private planServiceImpl planServiceImpl;
    @Autowired
    private planStateServiceImpl planStateServiceImpl;

    @GetMapping
    public Result getAllPlan(){
        log.info("查询所有计划");
        List<Plan> planList = planServiceImpl.getAllPlan();
        return Result.success(planList);
    }

    @PostMapping
    public Result addPlan(@RequestBody Plan plan){
        log.info("添加计划:{}", plan);
        int add = planServiceImpl.addPlan(plan);
        if(add > 0) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @DeleteMapping("/{planNo}")
    public Result deletePlan(@PathVariable String planNo){
        log.info("删除计划:{}", planNo);
        int delete = planServiceImpl.deletePlan(planNo);
        if(delete > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{planNo}")
    public Result updatePlan(@RequestBody Plan plan, @PathVariable String planNo){
        log.info("更新计划:{}", plan);
        int update = planServiceImpl.updatePlan(plan, planNo);
        if(update > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 统一状态变更接口
     * * @param planNo 生产计划编号 (planNo)
     * @param action 动作枚举 (如：PUBLISH, PAUSE, RESUME_WORK)
     */
    @PostMapping("/{planNo}/actions/{action}")
    public Result handleAction(@PathVariable String planNo, @PathVariable ActionEnum action, Integer userId) {
        try {
            log.info("用户{}申请对计划{}进行{}", userId, planNo, action);
            // 调用 Service 层进行状态流转编排
            // 内部会涉及：权限校验 -> 规则校验 -> 门禁校验 -> 状态执行 -> 审计记录
            planStateServiceImpl.handle(planNo, action, userId);

            return Result.success(String.format("计划 [%s] 执行动作 [%s] 成功", planNo, action.getDesc()));

        }catch (BusinessException e){
            //门禁校验失败
            return Result.error(e.getCode(), e.getMessage());
        } catch (SecurityException e){
            // 专门捕获 PermissionPolicy 抛出的权限异常,返回 403 状态码
            return Result.error(403, "权限不足: " + e.getMessage());
        } catch (IllegalStateException e) {
            // 业务逻辑/状态机规则冲突,例如：Plan 状态 [RUNNING] 不允许执行动作 [PUBLISH]
            return Result.error(400, "操作不符合规则: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            // 参数错误
            return Result.error(400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            // 未知系统异常
            log.error("真实异常类型: {}", e.getClass().getName());
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }
}
