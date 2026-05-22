package com.xtax.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * 统一返回结果封装类
 * 用于封装API接口的返回结果
 */
@Setter
@Getter
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result success(Object data){
        Result result = new Result();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static Result success(){
        Result result = new Result();
        result.setCode(200);
        result.setMessage("success");
        result.setData(null);
        return result;
    }

    public static Result error(String message){
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static Result error(Integer code, String message){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}
