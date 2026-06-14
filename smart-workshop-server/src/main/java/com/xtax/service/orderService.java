package com.xtax.service;

import com.xtax.dto.OrderDTO;
import com.xtax.dto.OrderUpdateDTO;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;

import java.util.List;

/**
 * 生产订单业务服务接口
 */
public interface orderService {

    /** 查询所有订单 */
    List<ProductionOrder> getAllOrder();

    /** 按计划编号查询订单列表 */
    List<ProductionOrder> getOrderByPlan(String planNo);

    /** 按订单编号查询 */
    ProductionOrder getOrderByNo(String orderNo);

    /** 新增订单（返回创建后的订单对象） */
    ProductionOrder addOrder(OrderDTO dto);

    /** 按状态感知规则修改订单 */
    int updateOrder(String orderNo, OrderUpdateDTO dto);

    /** 删除订单（仅 CREATED 状态可删） */
    int deleteOrder(String orderNo);

    /** 查询订单下的工单列表 */
    List<WorkOrder> getWorkOrdersByOrderNo(String orderNo);
}
