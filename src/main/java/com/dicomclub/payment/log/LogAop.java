package com.dicomclub.payment.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.dicomclub.payment.common.utils.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2022/11/17 0017 18:09
 */
@Component
@Aspect
@Slf4j
public class LogAop {


    /**
     * @Description: 定义需要拦截的切面
     * @Return: void
     **/

    //这样就可以在切面的同时排除指定的一些接口(不要直接两个表达式拼在一起)
    @Pointcut("execution(public * com.dicomclub.module.*.controller..*.*(..))")
    public void log() {

    }

    @Around("log()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] params = joinPoint.getArgs();
        try {
            HttpServletRequest req = ServletUtil.getRequest();
            if("GET".equals(req.getMethod())){
                log.info("get url -> {} ({})", ServletUtil.getFullUrl(req),getRemoteHost(req));
            }else{
                String paramsStr = JSON.toJSONString(getParams(req));
                log.info("post url -> {} ({}), params -> {}", req.getRequestURL(),getRemoteHost(req),paramsStr);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Object p = joinPoint.proceed(params);
        return p;
    }

    /**
     * 获取目标主机的ip
     * @param request
     * @return
     */
    private String getRemoteHost(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }






    private Map<String,Object> getParams(HttpServletRequest request) {
        Map combineResultMap = new HashMap();
        Map<String,Object> map = new HashMap<>();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        combineResultMap.putAll(map);
        if(!"POST".equals(request.getMethod())){
            return combineResultMap;
        }
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while(line != null){
                builder.append(line);
                line = reader.readLine();
            }
            String reqBody = builder.toString();
            Map<String,Object> map2 = JSON.parseObject(reqBody, Map.class);
            if(map2!=null){
                combineResultMap.putAll(map2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return combineResultMap;
    }




}