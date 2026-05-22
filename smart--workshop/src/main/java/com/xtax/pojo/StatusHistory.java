package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态历史实体类
 * 记录目标对象的状态变更历史
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusHistory {
    private Integer id;
    private String targetType;
    private String targetNo;
    private String oldStatus;
    private String newStatus;
    private Integer operatorId;
    private String createdTime;
}
