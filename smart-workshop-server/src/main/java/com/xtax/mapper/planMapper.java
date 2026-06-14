package com.xtax.mapper;

import com.xtax.entity.Plan;
import com.xtax.enums.StateEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface planMapper {
    //获取所有计划信息
    @Select("select * from production_plan")
    List<Plan> getAllPlan();

    //添加计划信息
    int addPlan(Plan plan);

    //根据计划编号查询计划信息
    @Select("select * from production_plan where plan_no = #{planNo}")
    Plan getPlanByNo(String planNo);

    //删除计划信息
    @Delete("delete from production_plan where plan_no = #{planNo}")
    int deletePlan(String planNo);

    //更新计划信息
    int updatePlan(@Param("plan") Plan plan,@Param("planNo") String planNo);

    //更新计划状态
    @Update("update production_plan set status = #{status} where plan_no = #{planNo}")
    void updatePlanStatus(@Param("planNo") String planNo,@Param("status") StateEnum status);

    //更新计划完成数量（所有订单完成时汇总）
    @Update("update production_plan set completed_num = #{completedNum}, update_time = now() where plan_no = #{planNo}")
    void updatePlanCompletedNum(@Param("planNo") String planNo, @Param("completedNum") Integer completedNum);
}
