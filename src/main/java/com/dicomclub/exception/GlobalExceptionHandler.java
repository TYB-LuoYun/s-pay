/**
 *
 */
package com.dicomclub.exception;

import com.dicomclub.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Administrator
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//	未知异常
	@ExceptionHandler(value = Exception.class)
    public Result doException(Exception e) {
		StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String trace = sw.toString();
        log.info(trace);
        log.error(trace);
    	return Result.error(e.getMessage(), null);
    }

//	业务异常
    @ExceptionHandler(ServiceException.class)
    public Result doServiceException(ServiceException e) {
    	log.info(e.getMessage());
    	return Result.error(e.getCode(), e.getMessage(), null);
    }


    /**
     * 处理所有RequestBody注解参数验证异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
    	 /*注意：此处的BindException 是 Spring 框架抛出的Validation异常*/
    	MethodArgumentNotValidException ex = (MethodArgumentNotValidException)e;

    	FieldError fieldError = ex.getBindingResult().getFieldError();
        if(fieldError!=null) log.warn("必填校验异常:{}({})", fieldError.getDefaultMessage(),fieldError.getField());
        e.printStackTrace();
        String errorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return this.error("参数校验不通过:"+errorMsg);
    }


    /**
     * 处理所有RequestParam注解数据验证异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if(fieldError!=null) log.warn("必填校验异常:{}({})", fieldError.getDefaultMessage(),fieldError.getField());
        ex.printStackTrace();
        String defaultMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return this.error("参数校验不通过:"+defaultMessage);
    }




    private Result error(String msg) {
        return Result.error(msg);
    }


}
