package com.xtax.service;

import com.xtax.dto.WorkOrderDTO;
import com.xtax.dto.WorkOrderUpdateDTO;
import com.xtax.entity.WorkOrder;

import java.util.List;

/**
 * 工单业务服务接口
 * 提供工单 CRUD 操作，遵循状态感知约束规则
 */
public interface workOrderService {

    /** 查询所有工单 */
    List<WorkOrder> getAllWorkOrders();

    /** 按工单编号查询 */
    WorkOrder getWorkOrderByNo(String workOrderNo);

    /** 按订单编号查询工单列表 */
    List<WorkOrder> getWorkOrderByOrder(String orderNo);

    /** 按人员 ID 查询工单列表 */
    List<WorkOrder> getWorkOrderByUser(Integer userId);

    /** 按工序 ID 查询工单列表 */
    List<WorkOrder> getWorkOrderByProcess(Integer processId);

    /** 新增工单（手工创建，初始状态 CREATED） */
    WorkOrder addWorkOrder(WorkOrderDTO dto);

    /** 按状态感知规则更新工单 */
    int updateWorkOrder(String workOrderNo, WorkOrderUpdateDTO dto);

    /** 删除工单（仅 CREATED 状态可删） */
    int deleteWorkOrder(String workOrderNo);
}
