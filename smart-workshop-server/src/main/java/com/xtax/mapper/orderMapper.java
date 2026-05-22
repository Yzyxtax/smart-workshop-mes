package com.xtax.mapper;

import com.xtax.entity.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface orderMapper {

    // 根据计划编号查询订单
    @Select("select * from production_order where plan_no = #{planNo}")
    List<ProductionOrder> getOrderByPlan(String planNo);

    // 查询所有订单
    @Select("select * from production_order")
    List<ProductionOrder> getAllOrder();
}
