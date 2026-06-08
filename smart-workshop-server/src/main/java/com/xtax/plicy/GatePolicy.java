package com.xtax.plicy;

import com.xtax.entity.Plan;
import com.xtax.entity.ProductionLine;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.lineMapper;
import com.xtax.mapper.planMapper;
import com.xtax.mapper.processFlowMapper;
import com.xtax.mapper.processMapper;
import com.xtax.mapper.userMapper;
import com.xtax.stateDomain.StateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GatePolicy implements GatePolicyInterface{
    @Autowired
    private planMapper planMapper;
    @Autowired
    private processFlowMapper processFlowMapper;
    @Autowired
    private lineMapper lineMapper;
    @Autowired
    private processMapper processMapper;
    @Autowired
    private userMapper userMapper;

    /**
     * 订单发布门禁：校验产线可用性 → 工艺完整性 → 人员可执行性
     *
     * @param context 状态上下文（bizNo = orderNo）
     * @param planNo  计划编号
     * @param lineNo  产线编号
     */
    @Override
    public void checkOrder(StateContext context, String planNo, String lineNo) {
        // 1. Capacity Gate：产线可用性校验
        ProductionLine line = lineMapper.getLine(lineNo);
        if (line == null) {
            throw new BusinessException("发布校验失败: 产线 " + lineNo + " 不存在");
        }
        if (!"空闲".equals(line.getLineStatus())) {
            throw new BusinessException("发布校验失败: 产线 " + lineNo + " 不可用或已被占用");
        }

        // 2. Process Gate：工艺完整性校验
        Integer flowId = line.getFlowId();
        if (flowId == null) {
            throw new BusinessException("发布校验失败: 产线 " + lineNo + " 未绑定有效工艺流程");
        }

        // 3. Skill Gate：人员可执行性校验
        List<Integer> processIdList = processFlowMapper.getProcessIdsByFlowId(flowId);
        if (processIdList == null || processIdList.isEmpty()) {
            throw new BusinessException("发布校验失败: 产线 " + lineNo + " 绑定的工艺流程无工序定义");
        }
        for (Integer processId : processIdList) {
            String processName = processMapper.getProcessById(processId);
            if (processName == null) {
                throw new BusinessException("发布校验失败: 工艺流程中存在无效工序（ID=" + processId + "）");
            }
            if (!userMapper.hasUserWithSkill(processName)) {
                throw new BusinessException("发布校验失败: 工序「" + processName + "」缺少具备技能的可用员工");
            }
        }
    }

    /**
     * 发布门禁校验：按 Capacity Gate → Process Gate → Skill Gate 顺序检查
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

        // 1. Process Gate：工艺流程存在性校验
        boolean hasProcessFlow = processFlowMapper.isFlowExist(bomId);
        if (!hasProcessFlow) {
            throw new BusinessException("发布失败：该产品对应的工艺流程未定义或无效");
        }

        // 2. Capacity Gate：产线可用性校验
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

        // 3. Skill Gate：人员技能校验
        // 遍历每个工艺流程下的每一道工序，确保至少有 1 名员工具备该工序技能
        for (Integer processFlowId : processFlowIdList) {
            List<Integer> processIdList = processFlowMapper.getProcessIdsByFlowId(processFlowId);
            for (Integer processId : processIdList) {
                String processName = processMapper.getProcessById(processId);
                if (processName == null) {
                    throw new BusinessException("发布失败：工艺流程中存在无效工序（ID=" + processId + "）");
                }
                if (!userMapper.hasUserWithSkill(processName)) {
                    throw new BusinessException("发布失败：工序「" + processName + "」缺少具备技能的可用员工");
                }
            }
        }
    }
}
