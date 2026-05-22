package com.xtax.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.xtax.stateDomain.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产计划实体类
 * 描述生产计划的详细信息，包括计划编号、名称、BOM、数量、时间等
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Plan {
    private String planNo;
    private String planName;
    private Integer bomId;
    private Integer planNum;
    private Integer completedNum;
    private LocalDate startTime;
    private LocalDate endTime;
    private String priority;
    private StateEnum status;
    private Integer creatorId;
    private Integer publisherId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String remark;
}