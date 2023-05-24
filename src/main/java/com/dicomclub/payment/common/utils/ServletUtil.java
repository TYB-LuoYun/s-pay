package com.dicomclub.payment.common.utils;


import com.alibaba.fastjson.JSON;
import com.dicomclub.payment.log.MyRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ftm
 * @date 2022/10/31 0031 17:55
 */
public class ServletUtil {


    public static MyRequestWrapper getWrapperRequest(HttpServletRequest request){
        // 获取请求body
        try {
            MyRequestWrapper myRequestWrapper = new MyRequestWrapper(request);
            return myRequestWrapper;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static  String getBody(HttpServletRequest request){
        // 获取请求body
        try {
            MyRequestWrapper myRequestWrapper = new MyRequestWrapper(request);
            return myRequestWrapper.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFullUrl(HttpServletRequest req) {
        return req.getRequestURL()+ (StringUtils.isBlank(req.getQueryString())==true?"":"?"+req.getQueryString());
    }

    /**
     * 获取url的根地址
     * @param req
     * @return
     */
    public static String getRootUrl(HttpServletRequest req) {
        String s = req.getRequestURI();
        Pattern p = Pattern.compile("([^:/])(/)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            int start = m.start(2);
            return s.substring(0,start);
        }else{
            return null;
        }
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name)
    {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        return parameter;
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {

        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return null;
        }
        return Integer.valueOf(parameter);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        return Integer.valueOf(parameter);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name)
    {

        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return null;
        }
        if(parameter=="1"||parameter.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        if(parameter=="1"||parameter.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest()
    {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string 待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }


    public static Map<String,Object> getParams(HttpServletRequest request) {
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
            }else{
                map.put(paramName, Arrays.asList(paramValues));
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

