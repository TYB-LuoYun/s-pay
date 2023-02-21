package com.dicomclub.support.advice;

import com.alibaba.fastjson.JSON;
import com.dicomclub.common.utils.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author admin
 */
@RestControllerAdvice(basePackages = "com.dicomclub")
public class JsonResponseBodyAdvice implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        String path = serverHttpRequest.getURI().getPath();

//        Annotation[] annotations = methodParameter.getDeclaringClass().getAnnotations();
//        if(annotations!=null&&annotations.length>0){
//            for (Annotation item: annotations ){
//                if(item.annotationType().equals(Dispatch.class)){
//                    return o;
//                }
//            }
//        }


        if (o instanceof Result) {
            return o;
        }

        if (o instanceof String) {
            //解决返回值为字符串时，不能正常包装
            return JSON.toJSONString(Result.success(o));
        }
        return Result.success(o);
    }
}
