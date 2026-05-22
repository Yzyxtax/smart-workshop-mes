package com.xtax.mapper;

import com.xtax.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface workOrderMapper {
    // 获取工单
    @Select("select * from work_order where order_no = #{orderNo}")
    List<WorkOrder> getWorkOrderByOrder(String orderNo);
}
