package com.dicomclub.payment.log;

import lombok.extern.slf4j.Slf4j;
import com.dicomclub.payment.common.utils.ServletUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 日志打印过滤器
 * @author ftm
 * @date 2022/11/18
 */
@Slf4j
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        MyRequestWrapper wrapperRequest = ServletUtil.getWrapperRequest(req);
//        if("GET".equals(req.getMethod())){
//            log.info("get url -> {}", ServletUtil.getFullUrl(req));
//        }else{
//            log.info("post url -> {}, params -> {}", req.getRequestURL(), JSON.toJSONString(wrapperRequest.getParams()));
//        }
        chain.doFilter(wrapperRequest, response);
    }


}
