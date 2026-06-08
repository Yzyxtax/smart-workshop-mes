package com.xtax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增订单请求体 DTO
 * 用于 POST /order 创建独立生产订单（非由计划联动生成）
 * 订单初始状态为 CREATED
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    /** 所属计划编号（必填） */
    private String planNo;
    /** 产线编号（必填） */
    private String lineNo;
    /** 订单名称（必填） */
    private String orderName;
    /** 计划生产数量（必填） */
    private Integer quantity;
    /** 计划开始时间 (yyyy-MM-dd HH:mm:ss)（可选） */
    private String startTime;
    /** 计划结束时间 (yyyy-MM-dd HH:mm:ss)（可选） */
    private String endTime;
    /** 备注（可选） */
    private String remark;
}
