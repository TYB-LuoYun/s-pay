package com.dicomclub.payment.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 用于封装接口统一响应结果
 */

/**
 * 用于封装接口统一响应结果
 */
@Data
@NoArgsConstructor // 无参构造方法
public final class Result implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Result.class);

    private static final long serialVersionUID = 1L;


    private Boolean success;

    /**
     * 响应业务状态码
     */
    private Integer httpCode;

    private String  code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应中的数据
     */
    private Object data;

    public Result(Boolean isSuccess, String code, String message, Object data) {
        this.success = isSuccess;
        this.code = code;
        if(isSuccess!=null&&isSuccess == true){
            this.httpCode = HttpCode.OK.value();
        }else {
            this.httpCode = HttpCode.INTERNAL_SERVER_ERROR.value();
        }
        this.message = message;
        this.data = data;
    }



    public Result(Boolean isSuccess, HttpCode code, String message, Object data) {
        this.success = isSuccess;
        this.code = ""+code.hashCode();
        this.httpCode = code.value();
        this.message = message;
        this.data = data;
    }




    public static Result success() {
        return new Result(true,ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getDesc(), null);
    }

    public static Result success(Object data) {
        return new Result(true,ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getDesc(), data);
    }


    public static Result success(String message, Object data) {
        return new Result(true,ResultEnum.SUCCESS.getCode(), message, data);
    }

    public static Result success(ResultEnum resultEnum) {
        return new Result(true,resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public static Result success(String message) {
        return new Result(true,ResultEnum.SUCCESS.getCode(), message, null);
    }

    public static Result error(String message, Object data) {
        return new Result(false, ResultEnum.ERROR.getCode(), message, data);
    }

    public static Result error(ResultEnum resultEnum, Object data) {
        return new Result(false, resultEnum.getCode(),resultEnum.getDesc(), data);
    }

    public static Result error(String code,String message, Object data) {
        return new Result(false,code, message, data);
    }

    public static Result error(HttpCode code,String message, Object data) {
        return new Result(false,code, message, data);
    }



    public static Result error(String message) {
        logger.debug("返回错误：code={}, message={}", ResultEnum.ERROR.getCode(), message);
        return new Result(false,ResultEnum.ERROR.getCode(), message, null);
    }





    public static Result build(String code, String message) {
        logger.debug("返回结果：code={}, message={}", code, message);
        return new Result(null,code, message, null);
    }

    public static Result build(ResultEnum resultEnum) {
        logger.debug("返回结果：code={}, message={}", resultEnum.getCode(), resultEnum.getDesc());
        return new Result(null,resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public static Result build(ResultEnum resultEnum,boolean isSuccess) {
        logger.debug("返回结果：code={}, message={}", resultEnum.getCode(), resultEnum.getDesc());
        return new Result(isSuccess,resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }
}