package com.xtax.service.serviceImpl;

import com.xtax.dto.WorkOrderDTO;
import com.xtax.dto.WorkOrderUpdateDTO;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.orderMapper;
import com.xtax.mapper.processMapper;
import com.xtax.mapper.userMapper;
import com.xtax.mapper.workOrderMapper;
import com.xtax.service.workOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 工单业务服务实现
 * 负责工单 CRUD 操作，遵循状态感知约束规则
 */
@Slf4j
@Service
public class workOrderServiceImpl implements workOrderService {

    /** 日期时间格式 */
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private workOrderMapper workOrderMapper;
    @Autowired
    private orderMapper orderMapper;
    @Autowired
    private processMapper processMapper;
    @Autowired
    private userMapper userMapper;

    @Override
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderMapper.getAllWorkOrders();
    }

    @Override
    public WorkOrder getWorkOrderByNo(String workOrderNo) {
        WorkOrder workOrder = workOrderMapper.getWorkOrderByNo(workOrderNo);
        if (workOrder == null) {
            throw new BusinessException("未找到编号为 " + workOrderNo + " 的工单");
        }
        return workOrder;
    }

    @Override
    public List<WorkOrder> getWorkOrderByOrder(String orderNo) {
        // 先校验订单是否存在
        if (orderMapper.getOrderByNo(orderNo) == null) {
            throw new BusinessException("未找到编号为 " + orderNo + " 的生产订单");
        }
        return workOrderMapper.getWorkOrderByOrder(orderNo);
    }

    @Override
    public List<WorkOrder> getWorkOrderByUser(Integer userId) {
        // 校验用户是否存在
        if (userMapper.getUserById(userId) == null) {
            throw new BusinessException("未找到用户 ID 为 " + userId + " 的人员");
        }
        return workOrderMapper.getWorkOrderByUser(userId);
    }

    @Override
    public List<WorkOrder> getWorkOrderByProcess(Integer processId) {
        // 校验工序是否存在
        if (processMapper.getProcessById(processId) == null) {
            throw new BusinessException("未找到 ID 为 " + processId + " 的工序");
        }
        return workOrderMapper.getWorkOrderByProcess(processId);
    }

    /**
     * 新增工单（手工创建）
     * 业务规则：
     *   - 工单编号自动生成，格式为 {orderNo}-{processId}-{seq}
     *   - 同一订单+工序+人员组合不可重复创建
     *   - 初始状态为 CREATED
     *   - 初始 actualQuantity、scrapQuantity 均为 0
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public WorkOrder addWorkOrder(WorkOrderDTO dto) {
        // 1. 校验必填字段
        if (dto.getOrderNo() == null || dto.getOrderNo().isBlank()) {
            throw new BusinessException("订单编号不能为空");
        }
        if (dto.getProcessId() == null) {
            throw new BusinessException("工序 ID 不能为空");
        }
        if (dto.getUserId() == null) {
            throw new BusinessException("派工人员 ID 不能为空");
        }
        if (dto.getPlannedQuantity() == null || dto.getPlannedQuantity() <= 0) {
            throw new BusinessException("计划数量必须大于 0");
        }

        // 2. 校验订单是否存在
        if (orderMapper.getOrderByNo(dto.getOrderNo()) == null) {
            throw new BusinessException("订单编号 " + dto.getOrderNo() + " 不存在");
        }

        // 3. 校验工序是否存在
        if (processMapper.getProcessById(dto.getProcessId()) == null) {
            throw new BusinessException("工序 ID " + dto.getProcessId() + " 不存在");
        }

        // 4. 校验人员是否存在
        if (userMapper.getUserById(dto.getUserId()) == null) {
            throw new BusinessException("人员 ID " + dto.getUserId() + " 不存在");
        }

        // 5. 检查同一订单+工序+人员组合是否已存在
        int existCount = workOrderMapper.countByOrderProcessUser(
                dto.getOrderNo(), dto.getProcessId(), dto.getUserId());
        if (existCount > 0) {
            throw new BusinessException("订单 " + dto.getOrderNo() + " 下工序 " + dto.getProcessId()
                    + " 已存在派工人员 " + dto.getUserId() + " 的工单，不可重复创建");
        }

        // 6. 生成工单编号：{orderNo}-{processId}-{seq}
        int seq = workOrderMapper.getMaxSeqByOrderAndProcess(dto.getOrderNo(), dto.getProcessId()) + 1;
        String workOrderNo = dto.getOrderNo() + "-" + dto.getProcessId() + "-" + seq;

        // 7. 构建工单对象
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo(workOrderNo);
        workOrder.setOrderNo(dto.getOrderNo());
        workOrder.setProcessId(dto.getProcessId());
        workOrder.setUserId(dto.getUserId());
        workOrder.setIsCritical(dto.getIsCritical() != null ? dto.getIsCritical() : false);
        workOrder.setPlannedQuantity(dto.getPlannedQuantity());
        workOrder.setActualQuantity(0);
        workOrder.setScrapQuantity(0);
        workOrder.setStatus(StateEnum.CREATED);
        workOrder.setRemark(dto.getRemark());

        // 解析可选时间字段
        if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
            workOrder.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
            workOrder.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
        }

        // 8. 插入数据库
        int rows = workOrderMapper.addWorkOrder(workOrder);
        if (rows <= 0) {
            throw new BusinessException("工单创建失败");
        }

        log.info("工单创建成功: {}", workOrderNo);
        return workOrderMapper.getWorkOrderByNo(workOrderNo);
    }

    /**
     * 按状态感知规则修改工单
     *
     * 修改规则：
     *   CREATED   — userId/processId/isCritical/plannedQuantity/时间/remark 全部可改
     *   RELEASED  — 仅 plannedQuantity/时间/remark 可改；userId/processId/isCritical 保持原值
     *   RUNNING   — 仅 actualQuantity/scrapQuantity/remark 可改（现场数据采集）
     *   PAUSED    — 仅 remark 可改
     *   COMPLETED — 不允许修改（终态）
     *   TERMINATED— 不允许修改（终态）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateWorkOrder(String workOrderNo, WorkOrderUpdateDTO dto) {
        // 1. 获取现有工单
        WorkOrder existing = workOrderMapper.getWorkOrderByNo(workOrderNo);
        if (existing == null) {
            throw new BusinessException("未找到编号为 " + workOrderNo + " 的工单");
        }

        StateEnum status = existing.getStatus();

        // 2. 构建更新对象（从现有数据出发）
        WorkOrder update = new WorkOrder();
        update.setWorkOrderNo(workOrderNo);

        switch (status) {
            case CREATED:
                // 草稿态：全部字段可修改
                if (dto.getUserId() != null) update.setUserId(dto.getUserId());
                if (dto.getProcessId() != null) update.setProcessId(dto.getProcessId());
                if (dto.getIsCritical() != null) update.setIsCritical(dto.getIsCritical());
                if (dto.getPlannedQuantity() != null) update.setPlannedQuantity(dto.getPlannedQuantity());
                if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
                    update.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
                }
                if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
                    update.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
                }
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case RELEASED:
                // 已发布：仅计划数量、时间、备注可改；userId/processId/isCritical 保持原值
                if (dto.getPlannedQuantity() != null) update.setPlannedQuantity(dto.getPlannedQuantity());
                if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
                    update.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
                }
                if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
                    update.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
                }
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case RUNNING:
                // 执行中：仅 actualQuantity、scrapQuantity、备注可改（现场数据采集）
                if (dto.getActualQuantity() != null) update.setActualQuantity(dto.getActualQuantity());
                if (dto.getScrapQuantity() != null) update.setScrapQuantity(dto.getScrapQuantity());
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case PAUSED:
                // 已暂停：仅备注可改
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case COMPLETED:
                throw new BusinessException("当前状态 [" + status.getDesc() + "] 不允许修改工单");

            case TERMINATED:
                throw new BusinessException("当前状态 [" + status.getDesc() + "] 不允许修改工单");
        }

        // 3. 执行更新
        int rows = workOrderMapper.updateWorkOrder(update);
        if (rows <= 0) {
            throw new BusinessException("工单更新失败");
        }

        log.info("工单 {} 修改成功（状态: {}）", workOrderNo, status.getDesc());
        return rows;
    }

    /**
     * 删除工单
     * 仅 CREATED 状态的工单允许删除
     * 设计原则：一旦工单已派工（RELEASED）或已产生作业事实，禁止删除，只允许作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteWorkOrder(String workOrderNo) {
        WorkOrder workOrder = workOrderMapper.getWorkOrderByNo(workOrderNo);
        if (workOrder == null) {
            throw new BusinessException("未找到编号为 " + workOrderNo + " 的工单");
        }

        if (workOrder.getStatus() != StateEnum.CREATED) {
            throw new BusinessException("当前状态 [" + workOrder.getStatus().getDesc()
                    + "] 不允许删除，仅创建状态的工单可删除");
        }

        int rows = workOrderMapper.deleteWorkOrderByNo(workOrderNo);
        log.info("工单 {} 删除成功", workOrderNo);
        return rows;
    }
}
