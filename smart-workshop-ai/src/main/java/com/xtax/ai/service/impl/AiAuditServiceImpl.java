package com.xtax.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.xtax.ai.dto.AiAuditQueryParam;
import com.xtax.ai.entity.AiAuditLog;
import com.xtax.ai.mapper.AiAuditLogMapper;
import com.xtax.ai.service.AiAuditService;
import com.xtax.vo.ResultPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 审计日志服务实现
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditServiceImpl implements AiAuditService {

    private final AiAuditLogMapper auditLogMapper;

    @Override
    public ResultPage<AiAuditLog> queryAuditLogs(AiAuditQueryParam param) {
        PageHelper.startPage(param.getPage(), param.getPageSize());
        List<AiAuditLog> list = auditLogMapper.selectByCondition(
                param.getUserId(), param.getToolName(),
                param.getStartTime(), param.getEndTime(),
                param.getSuccess());
        com.github.pagehelper.Page<AiAuditLog> page =
                (com.github.pagehelper.Page<AiAuditLog>) list;
        return new ResultPage<>(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAudit(AiAuditLog auditLog) {
        auditLogMapper.insert(auditLog);
        log.debug("记录 AI 审计: tool={}, success={}, duration={}ms",
                auditLog.getToolName(), auditLog.getSuccess(), auditLog.getDurationMs());
    }

    @Override
    public Map<String, Object> getToolStatistics(Integer days) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", auditLogMapper.countTotal());
        stats.put("successCount", auditLogMapper.countSuccess());
        stats.put("avgDuration", auditLogMapper.avgDuration());
        stats.put("byTool", auditLogMapper.countByTool(days));
        stats.put("byDay", auditLogMapper.countByDay(days));
        stats.put("byError", auditLogMapper.countByError(days));
        return stats;
    }
}
