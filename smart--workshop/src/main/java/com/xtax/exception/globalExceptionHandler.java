package com.xtax.exception;

import com.xtax.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class globalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("服务器异常：", e);
        return Result.error("服务器异常");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleException(DuplicateKeyException e) {
        log.error("服务器异常：", e);
        String message = e.getMessage();
        int i = message.indexOf("Duplicate entry");
        String errMsg = message.substring(i);
        String[] r = errMsg.split(" ");
        return Result.error(r[2] + "已存在");
    }
}
