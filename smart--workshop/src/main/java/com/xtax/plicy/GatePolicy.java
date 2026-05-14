package com.xtax.plicy;

import com.xtax.exception.BusinessException;
import com.xtax.mapper.lineMapper;
import com.xtax.mapper.planMapper;
import com.xtax.mapper.processFlowMapper;
import com.xtax.pojo.Plan;
import com.xtax.stateDomain.StateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class GatePolicy implements GatePolicyInterface{
    @Autowired
    private planMapper planMapper;
    @Autowired
    private processFlowMapper processFlowMapper;
    @Autowired
    private lineMapper lineMapper;

    /**
     * 统一门禁校验入口
     *
     * @param context  上下文
     * @throws BusinessException 如果门禁校验不通过则抛出异常
     */
    @Override
    public void check(StateContext context) {
        Plan plan = planMapper.getPlanByNo(context.getBizNo());
        if (plan == null) {
            throw new BusinessException("计划编号不存在");
        }

        Integer bomId = plan.getBomId();
        if (bomId == null) {
            throw new BusinessException("该计划未绑定产品(BOM)");
        }

        // 1. 校验工序流程是否存在
        boolean hasProcessFlow = processFlowMapper.isFlowExist(bomId);
        if (!hasProcessFlow) {
            throw new BusinessException("发布失败：该产品对应的工艺流程未定义或无效");
        }

        // 2. 校验是否有可用产线
        List<Integer> processFlowIdList = processFlowMapper.getProcessFlowIdList(bomId);
        boolean isLineExist = false;
        for (Integer processFlowId : processFlowIdList) {
            if (lineMapper.isLineExist(processFlowId)) {
                isLineExist = true;
                break;
            }
        }
        if(!isLineExist){
            throw new BusinessException("发布失败：工艺流程没有匹配的可用产线");
        }

        // 如果走到这里，说明全部校验通过，不抛出任何异常
    }
}
