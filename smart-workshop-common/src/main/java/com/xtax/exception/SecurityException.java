package com.xtax.exception;

/**
 * 权限校验异常
 * 当用户角色或身份不满足操作权限要求时抛出，由全局异常处理器映射为 HTTP 403 响应
 */
public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }
}
