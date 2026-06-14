package com.xtax.service.serviceImpl;

import com.xtax.dto.OrderDTO;
import com.xtax.dto.OrderUpdateDTO;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.lineMapper;
import com.xtax.mapper.orderMapper;
import com.xtax.mapper.planMapper;
import com.xtax.service.orderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 生产订单业务服务实现
 * 负责订单 CRUD 操作，遵循状态感知约束规则
 */
@Slf4j
@Service
public class orderServiceImpl implements orderService {

    /** 日期时间格式 */
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private orderMapper orderMapper;
    @Autowired
    private planMapper planMapper;
    @Autowired
    private lineMapper lineMapper;

    @Override
    public List<ProductionOrder> getAllOrder() {
        return orderMapper.getAllOrder();
    }

    @Override
    public List<ProductionOrder> getOrderByPlan(String planNo) {
        return orderMapper.getOrderByPlan(planNo);
    }

    @Override
    public ProductionOrder getOrderByNo(String orderNo) {
        ProductionOrder order = orderMapper.getOrderByNo(orderNo);
        if (order == null) {
            throw new BusinessException("未找到编号为 " + orderNo + " 的生产订单");
        }
        return order;
    }

    /**
     * 新增生产订单
     * 业务规则：
     *   - 订单编号自动生成，格式为 {planNo}-{lineNo}
     *   - 同一计划+产线组合不可重复创建
     *   - 初始状态为 CREATED
     *   - 初始生产数据均为 0
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductionOrder addOrder(OrderDTO dto) {
        // 1. 校验必填字段
        if (dto.getPlanNo() == null || dto.getPlanNo().isBlank()) {
            throw new BusinessException("计划编号不能为空");
        }
        if (dto.getLineNo() == null || dto.getLineNo().isBlank()) {
            throw new BusinessException("产线编号不能为空");
        }
        if (dto.getOrderName() == null || dto.getOrderName().isBlank()) {
            throw new BusinessException("订单名称不能为空");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("计划生产数量必须大于 0");
        }

        // 2. 校验计划是否存在
        if (planMapper.getPlanByNo(dto.getPlanNo()) == null) {
            throw new BusinessException("计划编号 " + dto.getPlanNo() + " 不存在");
        }

        // 3. 校验产线是否存在
        if (lineMapper.getLine(dto.getLineNo()) == null) {
            throw new BusinessException("产线编号 " + dto.getLineNo() + " 不存在");
        }

        // 4. 检查同一计划+产线组合是否已存在订单
        int existCount = orderMapper.countByPlanAndLine(dto.getPlanNo(), dto.getLineNo());
        if (existCount > 0) {
            throw new BusinessException("计划 " + dto.getPlanNo() + " 下产线 " + dto.getLineNo() + " 已存在订单，不可重复创建");
        }

        // 5. 构建订单对象
        ProductionOrder order = new ProductionOrder();
        String orderNo = dto.getPlanNo() + "-" + dto.getLineNo();
        order.setOrderNo(orderNo);
        order.setPlanNo(dto.getPlanNo());
        order.setLineNo(dto.getLineNo());
        order.setOrderName(dto.getOrderName());
        order.setQuantity(dto.getQuantity());
        order.setQuantityProduced(0);
        order.setQualifiedProducts(0);
        order.setDefectiveProducts(0);
        order.setStatus(StateEnum.CREATED);
        order.setRemark(dto.getRemark());

        // 解析可选时间字段
        if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
            order.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
            order.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
        }

        // 6. 插入数据库
        int rows = orderMapper.addOrder(order);
        if (rows <= 0) {
            throw new BusinessException("订单创建失败");
        }

        log.info("订单创建成功: {}", orderNo);
        return orderMapper.getOrderByNo(orderNo);
    }

    /**
     * 按状态感知规则修改订单
     *
     * 修改规则：
     *   CREATED   — 全部字段可修改
     *   RELEASED  — 仅 quantity、时间、remark 可修改；orderName/lineNo/planNo 保持原值
     *   RUNNING   — 不允许修改
     *   PAUSED    — 仅备注可修改
     *   COMPLETED — 不允许修改（终态）
     *   TERMINATED— 不允许修改（终态）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateOrder(String orderNo, OrderUpdateDTO dto) {
        // 1. 获取现有订单
        ProductionOrder existing = orderMapper.getOrderByNo(orderNo);
        if (existing == null) {
            throw new BusinessException("未找到编号为 " + orderNo + " 的生产订单");
        }

        StateEnum status = existing.getStatus();

        // 2. 构建更新对象（从现有数据出发）
        ProductionOrder update = new ProductionOrder();
        update.setOrderNo(orderNo);

        switch (status) {
            case CREATED:
                // 草稿态：全部字段可修改
                if (dto.getOrderName() != null) update.setOrderName(dto.getOrderName());
                if (dto.getLineNo() != null) update.setLineNo(dto.getLineNo());
                if (dto.getPlanNo() != null) update.setPlanNo(dto.getPlanNo());
                if (dto.getQuantity() != null) update.setQuantity(dto.getQuantity());
                if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
                    update.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
                }
                if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
                    update.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
                }
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case RELEASED:
                // 已发布：仅数量、时间、备注可改；名称/产线/计划保持原值
                if (dto.getQuantity() != null) update.setQuantity(dto.getQuantity());
                if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
                    update.setStartTime(LocalDateTime.parse(dto.getStartTime(), DT_FORMAT));
                }
                if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
                    update.setEndTime(LocalDateTime.parse(dto.getEndTime(), DT_FORMAT));
                }
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case PAUSED:
                // 已暂停：仅备注可改
                if (dto.getRemark() != null) update.setRemark(dto.getRemark());
                break;

            case RUNNING:
                throw new BusinessException("当前状态 [" + status.getDesc() + "] 不允许修改订单");

            case COMPLETED:
                throw new BusinessException("当前状态 [" + status.getDesc() + "] 不允许修改订单");

            case TERMINATED:
                throw new BusinessException("当前状态 [" + status.getDesc() + "] 不允许修改订单");
        }

        // 3. 执行更新
        int rows = orderMapper.updateOrder(update);
        if (rows <= 0) {
            throw new BusinessException("订单更新失败");
        }

        log.info("订单 {} 修改成功（状态: {}）", orderNo, status.getDesc());
        return rows;
    }

    /**
     * 删除订单
     * 仅 CREATED 状态的订单允许删除
     * 设计原则：一旦产生不可逆执行事实，禁止删除，只允许作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteOrder(String orderNo) {
        ProductionOrder order = orderMapper.getOrderByNo(orderNo);
        if (order == null) {
            throw new BusinessException("未找到编号为 " + orderNo + " 的生产订单");
        }

        if (order.getStatus() != StateEnum.CREATED) {
            throw new BusinessException("当前状态 [" + order.getStatus().getDesc()
                    + "] 不允许删除，仅创建状态的订单可删除");
        }

        int rows = orderMapper.deleteOrderByNo(orderNo);
        log.info("订单 {} 删除成功", orderNo);
        return rows;
    }

    @Override
    public List<WorkOrder> getWorkOrdersByOrderNo(String orderNo) {
        // 先确认订单存在
        if (orderMapper.getOrderByNo(orderNo) == null) {
            throw new BusinessException("未找到编号为 " + orderNo + " 的生产订单");
        }
        return orderMapper.getWorkOrdersByOrderNo(orderNo);
    }
}
