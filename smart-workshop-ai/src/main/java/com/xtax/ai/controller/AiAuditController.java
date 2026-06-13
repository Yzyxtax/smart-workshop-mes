package com.xtax.ai.controller;

import com.xtax.ai.dto.AiAuditQueryParam;
import com.xtax.ai.entity.AiAuditLog;
import com.xtax.ai.service.AiAuditService;
import com.xtax.vo.Result;
import com.xtax.vo.ResultPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 审计日志控制器。
 * 提供 AI 操作审计日志的查询功能。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@RestController
@RequestMapping("/ai/audit")
@RequiredArgsConstructor
public class AiAuditController {

    private final AiAuditService auditService;

    /**
     * 分页查询审计日志
     * GET /ai/audit?userId=&toolName=&startTime=&endTime=&success=&page=1&pageSize=10
     */
    @GetMapping
    public Result queryAuditLogs(AiAuditQueryParam param) {
        ResultPage<AiAuditLog> page = auditService.queryAuditLogs(param);
        return Result.success(page);
    }
}
