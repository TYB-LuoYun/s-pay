package com.dicomclub.payment.starter;

import com.dicomclub.payment.common.utils.RequestKitBean;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.service.PayContext;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.union.UnionPayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.v3.WxPayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.v3.service.WxPayAssistService;
import com.dicomclub.payment.util.httpRequest.HttpConfigStorage;
import com.dicomclub.payment.util.httpRequest.HttpRequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ftm
 * @date 2023/3/30 0030 9:51
 */
@Configuration
//// 当存在某个类时，此自动配置类才会生效
//@ConditionalOnClass({WxPayStrategy.class,   PayContext.class})
//@EnableConfigurationProperties(SPayProperties.class)

//导入外部@Bean配置的类
//@Import(WxPayConfig.class)
@ComponentScan(basePackages = "com.dicomclub.payment")
public class SPayAutoConfiguration {


//    @Bean
//    @ConditionalOnBean(WxPayConfig.class)
////    当你的bean被注册之后,如果而注册相同类型的bean,就不会成功,它会保证你的bean只有一个





    @Bean
    @ConditionalOnMissingBean(HttpRequestTemplate.class)
    public HttpRequestTemplate httpRequestTemplate(){
        //请求连接池配置
        HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
        //最大连接数
        httpConfigStorage.setMaxTotal(20);
        //默认的每个路由的最大连接数
        httpConfigStorage.setDefaultMaxPerRoute(10);
        HttpRequestTemplate requestTemplate = new HttpRequestTemplate(httpConfigStorage);
        return requestTemplate;
    }






}