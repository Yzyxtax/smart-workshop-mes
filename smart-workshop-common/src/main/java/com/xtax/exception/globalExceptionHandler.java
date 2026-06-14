package com.xtax.exception;

import com.xtax.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class globalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e, HttpServletResponse response) {
        log.error("业务异常：{}", e.getMessage());
        // 设置HTTP状态码，与业务异常码保持一致
        response.setStatus(e.getCode());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 权限校验异常处理器
     * 当用户角色或身份不满足操作权限要求时抛出，映射为 HTTP 403 响应
     */
    @ExceptionHandler(SecurityException.class)
    public Result handleSecurityException(SecurityException e, HttpServletResponse response) {
        log.error("权限异常：{}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return Result.error(403, "权限不足: " + e.getMessage());
    }

    /**
     * 参数校验异常处理器
     * 处理 @Valid 校验失败时抛出的 MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        log.error("参数校验失败：{}", e.getMessage());
        // 设置HTTP 400状态码
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        // 提取所有字段校验错误消息，拼接返回
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("服务器异常：", e);
        return Result.error("服务器异常");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据重复异常：", e);
        String message = e.getMessage();
        // 防御性解析：判断消息格式是否符合预期，避免二次异常
        if (message != null) {
            int i = message.indexOf("Duplicate entry");
            if (i >= 0) {
                String errMsg = message.substring(i);
                String[] r = errMsg.split(" ");
                if (r.length > 2) {
                    return Result.error(r[2] + "已存在");
                }
            }
        }
        // 兜底：解析失败时返回通用消息
        return Result.error("数据已存在，请勿重复添加");
    }

//    @ExceptionHandler(BadSqlGrammarException.class)
//    public Result handleException(BadSqlGrammarException e) {
//        log.error("服务器异常：", e);
//        return Result.error("数据库语法错误");
//    }
}
