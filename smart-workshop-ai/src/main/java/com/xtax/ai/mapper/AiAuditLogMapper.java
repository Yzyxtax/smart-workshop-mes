package com.xtax.ai.mapper;

import com.xtax.ai.entity.AiAuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * AI 操作审计日志 Mapper 接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Mapper
public interface AiAuditLogMapper {

    /**
     * 写入审计日志
     */
    @Insert("INSERT INTO ai_audit_log (session_id, message_id, user_id, tool_name, tool_params, " +
            "execution_result, target_type, target_id, success, error_message, duration_ms, " +
            "user_natural_input, created_at) " +
            "VALUES (#{sessionId}, #{messageId}, #{userId}, #{toolName}, #{toolParams}, " +
            "#{executionResult}, #{targetType}, #{targetId}, #{success}, #{errorMessage}, " +
            "#{durationMs}, #{userNaturalInput}, NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(AiAuditLog auditLog);

    /**
     * 分页查询审计日志
     */
    List<AiAuditLog> selectByCondition(@Param("userId") Integer userId,
                                        @Param("toolName") String toolName,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime,
                                        @Param("success") Boolean success);

    /**
     * 按工具统计调用次数
     */
    @Select("SELECT tool_name AS toolName, COUNT(*) AS count FROM ai_audit_log " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY tool_name ORDER BY count DESC")
    List<Map<String, Object>> countByTool(@Param("days") Integer days);

    /**
     * 按天统计工具调用次数
     */
    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count FROM ai_audit_log " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date ASC")
    List<Map<String, Object>> countByDay(@Param("days") Integer days);

    /**
     * 按错误类型统计
     */
    @Select("SELECT error_message AS errorType, COUNT(*) AS count FROM ai_audit_log " +
            "WHERE success = FALSE AND created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY error_message ORDER BY count DESC")
    List<Map<String, Object>> countByError(@Param("days") Integer days);

    /**
     * 统计总请求数
     */
    @Select("SELECT COUNT(*) FROM ai_audit_log")
    Long countTotal();

    /**
     * 统计成功数
     */
    @Select("SELECT COUNT(*) FROM ai_audit_log WHERE success = TRUE")
    Long countSuccess();

    /**
     * 计算平均延迟
     */
    @Select("SELECT AVG(duration_ms) FROM ai_audit_log WHERE duration_ms IS NOT NULL")
    Double avgDuration();
}
