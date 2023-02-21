package com.dicomclub.module.pay.service;

import com.dicomclub.exception.ServiceException;
import com.dicomclub.module.pay.config.AliPayConfig;
import com.dicomclub.module.pay.config.PayConfig;
import com.dicomclub.module.pay.config.UnionPayConfig;
import com.dicomclub.module.pay.config.WxPayConfig;
import com.dicomclub.module.pay.enums.PayType;
import com.dicomclub.module.pay.model.PayRequest;
import com.dicomclub.module.pay.model.PayResponse;
import com.dicomclub.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.module.pay.service.union.UnionPayStrategy;
import com.dicomclub.module.pay.service.wxpay.WxPayStrategy;
import com.dicomclub.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author ftm
 * @date 2023/2/15 0015 12:26
 * 策略类，根据条件选择不同的策略模式
 *
 * 需要进一步优化，因为支付对象会频繁创建
 */
@AllArgsConstructor
@Component
public class PayContext {


    /**
     * 多商户应用订单预支付
     * @param request
     * @param payConfig
     * @return
     */
    public PayResponse pay(PayRequest request , PayConfig payConfig) {
        if(request.getPayChannel() == null){
            throw new ServiceException("未知的支付渠道");
        }
        PayStrategy strategy = getPatStrategy(request.getPayChannel().getPayType(),payConfig);
        return strategy.pay(request);
    }




    /**
     * 固定商户应用订单预支付
     * @param request
     * @return
     */
    public PayResponse pay(PayRequest request) {
        PayConfig payConfig = getDefaultPayConfig(request.getPayChannel().getPayType());

        if(payConfig == null){
            throw new ServiceException("固定商户应用请全局配置支付环境参数");
        }
        return this.pay(request,payConfig);
    }




    /**
     * 支付结果异步通知,notifyData 是json或者xml串
     */
    public PayResponse asyncNotify(String notifyData){
        if (notifyData.startsWith("<xml>")) {
//          微信
            PayConfig payConfig = getDefaultPayConfig(PayType.WX);
            PayStrategy payStrategy = getPatStrategy(PayType.WX, payConfig);
            return payStrategy.asyncNotify(notifyData);
        } else {
            PayConfig payConfig = getDefaultPayConfig(PayType.ALIPAY);
            PayStrategy payStrategy = getPatStrategy(PayType.ALIPAY, payConfig);
            return payStrategy.asyncNotify(notifyData);
        }
    }







    /**
     * 根据支付平台获取默认的配置
     * @param payType
     * @return
     */
    private PayConfig getDefaultPayConfig(PayType payType) {
        PayConfig payConfig = null;
        switch (payType){
            case ALIPAY:
                payConfig = SpringContextUtil.getBeanNoException(AliPayConfig.class);
                break;
            case WX:
                payConfig = SpringContextUtil.getBeanNoException(WxPayConfig.class);
                break;
            case UNION:
                payConfig = SpringContextUtil.getBeanNoException(UnionPayConfig.class);
                break;
            default:
                throw new ServiceException("暂未实现该渠道对应的支付类型");
        }
        return payConfig;
    }

    /**
     * 根据支付类型 和 配置获取具体的策略类
     * @param payType
     * @param payConfig
     * @return
     */
    private PayStrategy getPatStrategy(PayType payType, PayConfig payConfig) {
        PayStrategy strategy = null;
        switch (payType){
            case ALIPAY:
                strategy = new AliPayStrategy((AliPayConfig)payConfig);
                break;
            case WX:
                strategy = new WxPayStrategy((WxPayConfig) payConfig);
                break;
            case UNION:
                strategy = new UnionPayStrategy((UnionPayConfig) payConfig);
                break;
            default:
                throw new ServiceException("暂未实现该渠道对应的支付类型");
        }
        return strategy;
    }









}
