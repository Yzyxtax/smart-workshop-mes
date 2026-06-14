package com.xtax.service.serviceImpl;

import com.xtax.mapper.planMapper;
import com.xtax.entity.Plan;
import com.xtax.service.planService;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class planServiceImpl implements planService {
    @Autowired
    private planMapper planMapper;

    //获取所有计划
    @Override
    public List<Plan> getAllPlan() {
        return planMapper.getAllPlan();
    }

    //添加计划
    @Override
    public int addPlan(Plan plan) {
        return planMapper.addPlan(plan);
    }

    //删除计划
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePlan(String planNo) {
        Plan plan = planMapper.getPlanByNo(planNo);
        if (plan == null) {
            throw new BusinessException("计划编号 " + planNo + " 不存在");
        }
        if (plan.getStatus().equals(StateEnum.CREATED)) {
            return planMapper.deletePlan(planNo);
        } else {
            throw new BusinessException("计划处于「" + plan.getStatus().getDesc() + "」状态，不允许删除");
        }
    }

    /**
     * 修改计划 — 按状态限制可修改字段
     * CREATED：全部字段可改
     * RELEASED：仅允许修改计划数量、时间、优先级、备注
     * 其余状态（RUNNING/PAUSED/COMPLETED/TERMINATED）：禁止修改
     */
    @Override
    public int updatePlan(Plan plan, String planNo) {
        Plan existing = planMapper.getPlanByNo(planNo);
        if (existing == null) {
            throw new BusinessException("计划编号 " + planNo + " 不存在");
        }
        StateEnum status = existing.getStatus();
        switch (status) {
            case CREATED:
                // 草稿态允许修改全部字段
                break;
            case RELEASED:
                // 已发布状态仅允许修改计划数量、时间、优先级、备注
                // BOM 和计划名称从原记录恢复，防止被覆盖
                plan.setBomId(existing.getBomId());
                plan.setPlanName(existing.getPlanName());
                break;
            case RUNNING:
                throw new BusinessException("计划处于「执行中」状态，不允许修改");
            case PAUSED:
                throw new BusinessException("计划处于「暂停」状态，不允许修改");
            case COMPLETED:
                throw new BusinessException("计划处于「完成」状态，不允许修改");
            case TERMINATED:
                throw new BusinessException("计划处于「作废」状态，不允许修改");
        }
        return planMapper.updatePlan(plan, planNo);
    }
}
