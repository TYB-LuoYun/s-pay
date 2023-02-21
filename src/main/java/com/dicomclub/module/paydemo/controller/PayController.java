package com.dicomclub.module.paydemo.controller;

import com.dicomclub.module.pay.config.AliPayConfig;
import com.dicomclub.module.pay.config.PayConfig;
import com.dicomclub.module.pay.config.WxPayConfig;
import com.dicomclub.module.pay.enums.PayChannel;
import com.dicomclub.module.pay.enums.PayType;
import com.dicomclub.module.pay.model.AllPayRequest;
import com.dicomclub.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.module.pay.service.PayContext;
import com.dicomclub.module.pay.service.PayStrategy;
import com.dicomclub.module.pay.service.alipay.AliPayStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ftm
 * @date 2023/2/16 0016 15:51
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private PayContext payContext;

    @RequestMapping("/test")
    public void test(@RequestBody AllPayRequest payRequest){
//      单商户 统一类型和 渠道 支付
        payContext.pay(payRequest.getPayRequest());

        /**
         * 单独使用支付宝策略 渠道支付
         */
        PayStrategy payStrategy = new AliPayStrategy(new AliPayConfig());
        payStrategy.pay(new AliPayRequest());


        /**
         * 模拟不同的商户应用的不同类型的不同渠道支付
         */
//        1参数支付渠道，商户，需要有一个 根据支付渠道，和商户获取支付配置的接口，这个涉及数据库查询，所以不予以实现，下面是伪代码
        PayConfig payConfig = getConfig(payRequest.getPayChannel().getPayType(),"商户id");
        payContext.pay(payRequest.getPayRequest(),payConfig);





    }

    private PayConfig getConfig(PayType type, String 商户id) {
        if(PayType.WX == type){
            return new WxPayConfig();
        }
        if(PayType.ALIPAY == type) {
            AliPayConfig payConfig = new AliPayConfig();
            payConfig.setAppId("2333");
            return payConfig;
        }
        return null;

    }
}
