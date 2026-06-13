package com.xtax.ai.service;

import com.xtax.ai.dto.AiAuditQueryParam;
import com.xtax.ai.entity.AiAuditLog;
import com.xtax.vo.ResultPage;

import java.util.Map;

/**
 * AI 审计日志服务接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface AiAuditService {

    /**
     * 分页查询审计日志
     *
     * @param param 查询参数
     * @return 分页结果
     */
    ResultPage<AiAuditLog> queryAuditLogs(AiAuditQueryParam param);

    /**
     * 记录一次工具调用审计
     *
     * @param auditLog 审计日志实体
     */
    void recordAudit(AiAuditLog auditLog);

    /**
     * 获取工具调用统计（按工具分组）
     *
     * @param days 统计天数
     * @return 统计数据
     */
    Map<String, Object> getToolStatistics(Integer days);
}
