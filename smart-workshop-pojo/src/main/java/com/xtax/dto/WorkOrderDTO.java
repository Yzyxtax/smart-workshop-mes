package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增工单请求体 DTO
 * 用于 POST /workOrder 手工创建工单（非由订单发布联动生成）
 * 工单初始状态为 CREATED
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderDTO {
    /** 所属订单编号（必填） */
    private String orderNo;
    /** 工序 ID（必填） */
    private Integer processId;
    /** 派工人员 ID（必填） */
    private Integer userId;
    /** 是否关键工单（默认 false），关键工单影响订单状态聚合 */
    private Boolean isCritical;
    /** 计划数量（必填） */
    private Integer plannedQuantity;
    /** 计划开始时间 (yyyy-MM-dd HH:mm:ss)（可选） */
    private String startTime;
    /** 计划结束时间 (yyyy-MM-dd HH:mm:ss)（可选） */
    private String endTime;
    /** 备注（可选） */
    private String remark;
}
