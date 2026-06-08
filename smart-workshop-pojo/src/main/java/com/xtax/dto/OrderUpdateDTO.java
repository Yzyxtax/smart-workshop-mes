package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改订单请求体 DTO
 * 用于 PUT /order/{orderNo} 修改订单信息
 * 根据订单当前状态，可修改的字段范围不同（状态感知更新规则）
 *
 * CREATED：全部字段可改
 * RELEASED：仅 quantity / 时间 / remark 可改，orderName/lineNo/planNo 保持原值
 * RUNNING / COMPLETED / TERMINATED：不允许修改
 * PAUSED：仅 remark 可改
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateDTO {
    /** 订单名称（仅 CREATED 状态可改） */
    private String orderName;
    /** 产线编号（仅 CREATED 状态可改） */
    private String lineNo;
    /** 计划编号（仅 CREATED 状态可改） */
    private String planNo;
    /** 计划生产数量 */
    private Integer quantity;
    /** 计划开始时间 (yyyy-MM-dd HH:mm:ss) */
    private String startTime;
    /** 计划结束时间 (yyyy-MM-dd HH:mm:ss) */
    private String endTime;
    /** 备注 */
    private String remark;
}
