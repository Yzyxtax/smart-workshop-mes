-- ==========================================
-- AI 智能交互模块 - 数据库建表 DDL
-- 版本: v1.0
-- 日期: 2026-06-13
-- 数据库: smart_workshop
-- ==========================================

-- AI 对话会话表
CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '会话归属用户',
    title VARCHAR(100) DEFAULT '' COMMENT '会话标题（首条消息前50字符自动生成）',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / ARCHIVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    INDEX idx_user_id (user_id),
    INDEX idx_updated_at (updated_at DESC),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话会话';

-- 对话消息表
CREATE TABLE IF NOT EXISTS ai_chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '所属会话',
    role VARCHAR(20) NOT NULL COMMENT 'user / assistant / system / tool',
    content TEXT NOT NULL COMMENT '消息内容（Markdown格式，tool角色时为JSON）',
    tool_calls JSON NULL COMMENT 'AI 请求的工具调用列表 [{name, params, result}]',
    token_usage JSON NULL COMMENT 'token 消耗 {prompt, completion, total}',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id, created_at),
    CONSTRAINT fk_ai_msg_session FOREIGN KEY (session_id)
        REFERENCES ai_chat_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话消息';

-- AI 操作审计日志表
CREATE TABLE IF NOT EXISTS ai_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '关联会话',
    message_id BIGINT NOT NULL COMMENT '关联用户消息',
    user_id INT NOT NULL COMMENT '操作人（JWT 中的 userId）',
    tool_name VARCHAR(100) NOT NULL COMMENT '调用的工具名称',
    tool_params JSON NULL COMMENT '工具参数（脱敏后）',
    execution_result TEXT NULL COMMENT '执行结果（成功时为返回值JSON，失败时为错误信息）',
    target_type VARCHAR(50) NULL COMMENT '操作目标类型（表名或实体名）',
    target_id VARCHAR(100) NULL COMMENT '操作目标 ID',
    success TINYINT(1) NOT NULL COMMENT '是否成功',
    error_message VARCHAR(500) NULL COMMENT '失败时的错误信息',
    duration_ms INT NULL COMMENT '执行耗时（毫秒）',
    user_natural_input TEXT NULL COMMENT '用户原始自然语言输入',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计时间',
    INDEX idx_user_id (user_id, created_at),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    INDEX idx_tool_name (tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 操作审计日志';

-- 工具注册表（管理用，可选）
CREATE TABLE IF NOT EXISTS ai_tool_registry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_name VARCHAR(100) NOT NULL UNIQUE COMMENT '工具名称',
    tool_label VARCHAR(100) NULL COMMENT '工具中文标签',
    category VARCHAR(50) NULL COMMENT '工具分类',
    permissions JSON NULL COMMENT '所需权限编码列表',
    schema_definition JSON NULL COMMENT 'JSON Schema 完整定义',
    requires_confirmation TINYINT(1) DEFAULT 0 COMMENT '是否需要二次确认',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用（禁用后不注册到LLM）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 工具注册表';
