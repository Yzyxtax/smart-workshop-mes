package com.xtax.service.serviceImpl;

import com.xtax.mapper.planMapper;
import com.xtax.pojo.Plan;
import com.xtax.service.planService;
import com.xtax.stateDomain.StateEnum;
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
        if(plan.getStatus().equals(StateEnum.CREATED)){
            return planMapper.deletePlan(planNo);
        }else{
            return 0;
        }
    }

    //修改计划
    @Override
    public int updatePlan(Plan plan, String planNo) {
        return planMapper.updatePlan(plan, planNo);
    }
}
