package com.dicomclub.payment.module.pay.service;

import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.enums.PayType;
import com.dicomclub.payment.module.pay.model.OrderQueryRequest;
import com.dicomclub.payment.module.pay.model.OrderQueryResponse;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.union.UnionPayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.WxPayStrategy;
import com.dicomclub.payment.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ftm
 * @date 2023/2/15 0015 12:26
 * 策略类，根据条件选择不同的策略模式
 *
 * 所有的支付类别对象设计为单例模式，避免频繁创建对象
 */
@AllArgsConstructor
@Component
public class PayContext {

    @Autowired
    private AliPayStrategy aliPayStrategy;
    @Autowired
    private WxPayStrategy wxPayStrategy;
    @Autowired
    private UnionPayStrategy unionPayStrategy;


    /**
     * 多商户应用订单预支付
     * @param request
     * @param payConfig
     * @return
     */
    public PayResponse pay(PayRequest request , PayConfig payConfig) {
        if(request.getPayChannel() == null){
            throw new PayException("未知的支付渠道");
        }
        PayStrategy strategy = getPatStrategy(request.getPayChannel().getPayType() );
        return strategy.pay(request,payConfig);
    }




    /**
     * 固定商户应用订单预支付
     * @param request
     * @return
     */
    public PayResponse pay(PayRequest request) {
        PayConfig payConfig = getDefaultPayConfig(request.getPayChannel().getPayType());

        if(payConfig == null){
            throw new PayException("固定商户应用请全局配置支付环境参数");
        }
        return this.pay(request,payConfig);
    }




    /**
     * 支付结果异步通知,notifyData 是json或者xml串
     */
    public PayResponse asyncNotify(String notifyData,PayConfig payConfig){
        if (notifyData.startsWith("<xml>")) {
//          微信
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.WX);
            }
            PayStrategy payStrategy = getPatStrategy(PayType.WX);
            return payStrategy.asyncNotify(notifyData,payConfig);
        } else {
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.ALIPAY);
            }
            PayStrategy payStrategy = getPatStrategy(PayType.ALIPAY);
            return payStrategy.asyncNotify(notifyData,payConfig);
        }
    }

    public PayResponse asyncNotify(String notifyData){
        return this.asyncNotify(notifyData,null );
    }



    public PayResponse asyncNotify(String notifyData,PayConfig payConfig,PayType payType){
        if (PayType.WX == payType) {
//          微信
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.WX);
            }
            PayStrategy payStrategy = getPatStrategy(PayType.WX);
            return payStrategy.asyncNotify(notifyData,payConfig);
        } else  if(PayType.ALIPAY == payType){
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.ALIPAY);
            }
            PayStrategy payStrategy = getPatStrategy(PayType.ALIPAY);
            return payStrategy.asyncNotify(notifyData,payConfig);
        }else if(PayType.UNION == payType){
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.UNION);
            }
            PayStrategy payStrategy = getPatStrategy(PayType.UNION);
            return payStrategy.asyncNotify(notifyData,payConfig);
        }else{
            return null;
        }

    }



    public OrderQueryResponse query(OrderQueryRequest request, PayConfig payConfig){
        PayType payType = request.getPayType();
        if(request.getPayType() == null){
            throw new PayException("未知的支付方式");
        }
        PayStrategy payStrategy = null;
        if (PayType.WX == payType) {
//          微信
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.WX);
            }
            payStrategy = getPatStrategy(PayType.WX);
        } else if(PayType.ALIPAY == payType){
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.ALIPAY);
            }
            payStrategy = getPatStrategy(PayType.ALIPAY);
        }else if(PayType.UNION == payType){
            if(payConfig == null){
                payConfig = getDefaultPayConfig(PayType.UNION);
            }
            payStrategy = getPatStrategy(PayType.UNION);
        }else{
            throw new PayException("暂不支持该支付方式");
        }
        return payStrategy.query(request,payConfig);
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
                throw new PayException("暂未实现该渠道对应的支付类型");
        }
        return payConfig;
    }

    /**
     * 根据支付类型 和 配置获取具体的策略类
     * @param payType
     * @return
     */
    private PayStrategy getPatStrategy(PayType payType ) {
        PayStrategy strategy = null;
        switch (payType){
            case ALIPAY:
                strategy = aliPayStrategy;
                break;
            case WX:
                strategy = wxPayStrategy;
                break;
            case UNION:
                strategy = unionPayStrategy;
                break;
            default:
                throw new PayException("暂未实现该渠道对应的支付类型");
        }
        return strategy;
    }









}
