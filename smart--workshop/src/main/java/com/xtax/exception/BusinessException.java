package com.xtax.exception;

public class BusinessException extends RuntimeException{
    private Integer code;
    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认业务错误码
    }
    public Integer getCode() { return code; }
}
