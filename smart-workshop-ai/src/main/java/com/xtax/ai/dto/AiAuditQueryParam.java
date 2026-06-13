package com.xtax.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 审计日志查询参数
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiAuditQueryParam {

    /** 用户 ID 筛选 */
    private Integer userId;

    /** 工具名称筛选 */
    private String toolName;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 是否仅查询成功的记录 */
    private Boolean success;

    /** 页码（默认 1） */
    private Integer page = 1;

    /** 每页条数（默认 10） */
    private Integer pageSize = 10;
}
