package com.xtax.service;

import com.xtax.entity.ProductionOrder;

import java.util.List;

public interface orderService {
    //获取计划包含的订单
    List<ProductionOrder> getOrderByPlan(String planNo);

    //获取所有订单
    Object getAllOrder();
}
