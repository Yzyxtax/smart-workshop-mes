package com.xtax.mapper;

import com.xtax.entity.WorkOrder;
import com.xtax.enums.StateEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 工单数据访问层
 * 提供工单 CRUD 及状态变更的数据库操作
 */
@Mapper
public interface workOrderMapper {

    /** 查询所有工单 */
    @Select("select * from work_order order by create_time desc")
    List<WorkOrder> getAllWorkOrders();

    /** 按工单编号查询 */
    @Select("select * from work_order where work_order_no = #{workOrderNo}")
    WorkOrder getWorkOrderByNo(String workOrderNo);

    /** 按订单编号查询工单列表 */
    @Select("select * from work_order where order_no = #{orderNo} order by create_time")
    List<WorkOrder> getWorkOrderByOrder(String orderNo);

    /** 按人员 ID 查询工单列表 */
    @Select("select * from work_order where user_id = #{userId} order by create_time desc")
    List<WorkOrder> getWorkOrderByUser(Integer userId);

    /** 按工序 ID 查询工单列表 */
    @Select("select * from work_order where process_id = #{processId} order by create_time desc")
    List<WorkOrder> getWorkOrderByProcess(Integer processId);

    /** 添加工单 */
    @Insert("insert into work_order (work_order_no, order_no, process_id, user_id, is_critical, "
            + "planned_quantity, actual_quantity, scrap_quantity, status, remark, "
            + "start_time, end_time, actual_start_time, actual_end_time, create_time, update_time) "
            + "values (#{workOrderNo}, #{orderNo}, #{processId}, #{userId}, #{isCritical}, "
            + "#{plannedQuantity}, #{actualQuantity}, #{scrapQuantity}, #{status}, #{remark}, "
            + "#{startTime}, #{endTime}, #{actualStartTime}, #{actualEndTime}, now(), now())")
    int addWorkOrder(WorkOrder workOrder);

    /** 按状态感知规则更新工单（动态 SQL，XML 实现） */
    int updateWorkOrder(WorkOrder workOrder);

    /** 仅更新工单状态 */
    @Update("update work_order set status = #{status}, update_time = now() where work_order_no = #{workOrderNo}")
    int updateWorkOrderStatus(@Param("workOrderNo") String workOrderNo, @Param("status") StateEnum status);

    /** 更新工单状态 + 实际开始时间（员工 START_WORK 时锚定） */
    @Update("update work_order set status = #{status}, actual_start_time = now(), update_time = now() where work_order_no = #{workOrderNo}")
    int updateWorkOrderStatusAndStartTime(@Param("workOrderNo") String workOrderNo, @Param("status") StateEnum status);

    /** 更新工单状态 + 实际结束时间（员工 FINISH_WORK 时锚定） */
    @Update("update work_order set status = #{status}, actual_end_time = now(), update_time = now() where work_order_no = #{workOrderNo}")
    int updateWorkOrderStatusAndEndTime(@Param("workOrderNo") String workOrderNo, @Param("status") StateEnum status);

    /** 按工单编号删除 */
    @Delete("delete from work_order where work_order_no = #{workOrderNo}")
    int deleteWorkOrderByNo(String workOrderNo);

    /** 检查同一订单+工序+人员组合是否已存在（防止重复创建） */
    @Select("select count(*) from work_order where order_no = #{orderNo} and process_id = #{processId} and user_id = #{userId}")
    int countByOrderProcessUser(@Param("orderNo") String orderNo, @Param("processId") Integer processId, @Param("userId") Integer userId);

    /** 获取同一订单+工序下的最大序号，用于生成工单编号 */
    @Select("select count(*) from work_order where order_no = #{orderNo} and process_id = #{processId}")
    int getMaxSeqByOrderAndProcess(@Param("orderNo") String orderNo, @Param("processId") Integer processId);

    /** 查询订单下所有关键工单 */
    @Select("select * from work_order where order_no = #{orderNo} and is_critical = true")
    List<WorkOrder> getCriticalWorkOrdersByOrder(String orderNo);

    /** 查询订单下所有工单 */
    @Select("select * from work_order where order_no = #{orderNo}")
    List<WorkOrder> getWorkOrdersByOrderNo(String orderNo);
}
