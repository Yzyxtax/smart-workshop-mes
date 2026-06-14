package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改工单请求体 DTO
 * 用于 PUT /workOrder/{workOrderNo} 修改工单信息
 * 根据工单当前状态，可修改的字段范围不同（状态感知更新规则）
 *
 * CREATED   — userId/processId/isCritical/plannedQuantity/时间/remark 全部可改
 * RELEASED  — 仅 plannedQuantity/时间/remark 可改；userId/processId/isCritical 保持原值
 * RUNNING   — 仅 actualQuantity/scrapQuantity/remark 可改（现场数据采集）
 * PAUSED    — 仅 remark 可改
 * COMPLETED — 不允许修改（终态）
 * TERMINATED— 不允许修改（终态）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderUpdateDTO {
    /** 派工人员 ID（仅 CREATED 状态可改） */
    private Integer userId;
    /** 工序 ID（仅 CREATED 状态可改） */
    private Integer processId;
    /** 是否关键工单（仅 CREATED 状态可改） */
    private Boolean isCritical;
    /** 计划数量 */
    private Integer plannedQuantity;
    /** 实际完成数量（RUNNING 状态可由员工上报） */
    private Integer actualQuantity;
    /** 报废数量（RUNNING 状态可由员工上报） */
    private Integer scrapQuantity;
    /** 计划开始时间 (yyyy-MM-dd HH:mm:ss) */
    private String startTime;
    /** 计划结束时间 (yyyy-MM-dd HH:mm:ss) */
    private String endTime;
    /** 备注 */
    private String remark;
}
