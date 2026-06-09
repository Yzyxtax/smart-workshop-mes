package com.xtax.mapper;

import com.xtax.entity.ProductionOrder;
import com.xtax.enums.StateEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface orderMapper {

    // 根据计划编号查询订单
    @Select("select * from production_order where plan_no = #{planNo}")
    List<ProductionOrder> getOrderByPlan(String planNo);

    // 查询所有订单
    @Select("select * from production_order")
    List<ProductionOrder> getAllOrder();

    // 根据订单编号查询订单
    @Select("select * from production_order where order_no = #{orderNo}")
    ProductionOrder getOrderByNo(String orderNo);

    // 添加生产订单
    int addOrder(ProductionOrder order);

    // 更新订单信息（动态 SQL，按状态感知规则只更新允许变更的字段）
    int updateOrder(ProductionOrder order);

    // 删除订单
    @Delete("delete from production_order where order_no = #{orderNo}")
    int deleteOrderByNo(String orderNo);

    // 检查同一计划+产线组合是否已存在订单（防止重复创建）
    @Select("select count(*) from production_order where plan_no = #{planNo} and line_no = #{lineNo}")
    int countByPlanAndLine(@Param("planNo") String planNo, @Param("lineNo") String lineNo);

    // 更新订单状态
    @Update("update production_order set status = #{status} where order_no = #{orderNo}")
    void updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") StateEnum status);

    // 更新订单状态 + 实际开始时间
    @Update("update production_order set status = #{status}, actual_start_time = now() where order_no = #{orderNo}")
    void updateOrderStatusAndStartTime(@Param("orderNo") String orderNo, @Param("status") StateEnum status);

    // 更新订单状态 + 实际结束时间
    @Update("update production_order set status = #{status}, actual_end_time = now() where order_no = #{orderNo}")
    void updateOrderStatusAndEndTime(@Param("orderNo") String orderNo, @Param("status") StateEnum status);

    // 根据订单编号查询其下工单列表
    @Select("select * from work_order where order_no = #{orderNo}")
    List<com.xtax.entity.WorkOrder> getWorkOrdersByOrderNo(String orderNo);

    // 添加工单（订单发布联动时调用）
    @Insert("insert into work_order (work_order_no, order_no, process_id, user_id, is_critical, planned_quantity, actual_quantity, scrap_quantity, status, start_time, end_time, actual_start_time, actual_end_time, create_time, update_time) "
            + "values (#{workOrderNo}, #{orderNo}, #{processId}, #{userId}, #{isCritical}, #{plannedQuantity}, #{actualQuantity}, #{scrapQuantity}, #{status}, #{startTime}, #{endTime}, #{actualStartTime}, #{actualEndTime}, now(), now())")
    void insertWorkOrder(com.xtax.entity.WorkOrder workOrder);

    // 更新工单状态（订单取消发布联动时使用）
    @Update("update work_order set status = #{status}, update_time = now() where work_order_no = #{workOrderNo}")
    void updateWorkOrderStatus(@Param("workOrderNo") String workOrderNo, @Param("status") StateEnum status);
}
