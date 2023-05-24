package com.dicomclub.payment.log;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ftm
 * @date 2022/11/7 0007 18:14
 */
public class MyRequestWrapper extends HttpServletRequestWrapper {
    private String body;
    public MyRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = getBodyString(request);

    }


    public String getBodyString(final HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        String bodyString = "";
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        if (StringUtils.isNotBlank(contentType) && (contentType.contains("multipart/form-data") || contentType.contains("x-www-form-urlencoded"))) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            for (Map.Entry<String, String[]> next : parameterMap.entrySet()) {
                String[] values = next.getValue();
                String value = null;
                if (values != null) {
                    if (values.length == 1) {
                        value = values[0];
                    } else {
                        value = Arrays.toString(values);
                    }
                }
//                sb.append(next.getKey()).append("=").append(value).append("&");
                map.put(next.getKey(), value);
            }
            if (map !=null ) {
                bodyString = JSON.toJSONString(map);
            }
            return bodyString;
        } else {
            return IOUtils.toString(request.getInputStream());
        }
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }
            @Override
            public boolean isReady() {
                return false;
            }
            @Override
            public int read() {
                return bais.read();
            }
            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }


    public Map<String,Object> getParams() {
        Map combineResultMap = new HashMap();
        Map<String,Object> map = new HashMap<>();
        Enumeration paramNames = this.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = this.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        combineResultMap.putAll(map);
        if(!"POST".equals(this.getMethod())){
            return combineResultMap;
        }
        try {
            String reqBody = getBody();
            Map<String,Object> map2 = JSON.parseObject(reqBody, Map.class);
            if(map2!=null){
                combineResultMap.putAll(map2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

        return combineResultMap;
    }



    public String getBody() {
        return body;
    }

    // 赋值给body字段
    public void setBody(String body) {
        this.body = body;
    }

}
