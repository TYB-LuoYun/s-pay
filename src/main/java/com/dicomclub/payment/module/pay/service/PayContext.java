package com.dicomclub.payment.module.pay.service;

import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayType;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.union.UnionPayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.WxV2PayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.v3.WxPayStrategy;
import com.dicomclub.payment.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

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

    private static AliPayStrategy aliPayStrategy;

    private static WxV2PayStrategy wxV2PayStrategy;

    private static UnionPayStrategy unionPayStrategy;

    private static WxPayStrategy wxPayStrategy;


    @Autowired
    public void setAliContext(AliPayStrategy context) {
        PayContext.aliPayStrategy = context;
    }
    @Autowired
    public void setWxontext(WxPayStrategy context) {
        PayContext.wxPayStrategy = context;
    }
    @Autowired
    public void setUnionContext(UnionPayStrategy context) {
        PayContext.unionPayStrategy = context;
    }
    @Autowired
    public void setWxV2Context(WxV2PayStrategy context) {
        PayContext.wxV2PayStrategy = context;
    }


























//
//
//
//    /**
//     * 多商户应用订单预支付
//     * @param request
//     * @param payConfig
//     * @return
//     */
//    public PayResponse pay(PayRequest request , PayConfig payConfig) {
//        if(request.getPayChannel() == null){
//            throw new PayException("未知的支付渠道");
//        }
//        PayStrategy strategy = getPatStrategy(request.getPayChannel().getPayType() );
//        return strategy.pay(request,payConfig);
//    }
//
//
//    /**
//     * 退款
//     */
//    public RefundResponse refund(RefundRequest request , PayConfig payConfig) {
//        if(request.getPayChannel() == null){
//            throw new PayException("未知的支付渠道");
//        }
//        PayStrategy strategy = getPatStrategy(request.getPayChannel().getPayType() );
//        return strategy.refund(request,payConfig);
//    }
//
//
//    /**
//     * 退款
//     */
//    public RefundResponse refundQuery(RefundQueryRequest request , PayConfig payConfig) {
//        PayStrategy strategy = getPatStrategy(getPayType(payConfig) );
//        return strategy.refundQuery(request,payConfig);
//    }
//
//
//
//
//    /**
//     * 固定商户应用订单预支付
//     * @param request
//     * @return
//     */
//    public PayResponse pay(PayRequest request) {
//        PayConfig payConfig = getDefaultPayConfig(request.getPayChannel().getPayType());
//
//        if(payConfig == null){
//            throw new PayException("固定商户应用请全局配置支付环境参数");
//        }
//        return this.pay(request,payConfig);
//    }
//
//
//
//
//    /**
//     * 支付结果异步通知,notifyData 是json或者xml串
//     */
//    public PayResponse asyncNotify(HttpServletRequest request, PayConfig payConfig){
//        String notifyData = null;
//        try {
//            notifyData = request.getReader().lines().collect(Collectors.joining());
//        } catch (IOException e) {
//            throw new PayException(e.getMessage());
//        }
//        if (notifyData.startsWith("<xml>")) {
////          微信
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.WX);
//            }
//            PayStrategy payStrategy = getPatStrategy(PayType.WX);
//            return payStrategy.asyncNotify(request,payConfig);
//        } else {
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.ALIPAY);
//            }
//            PayStrategy payStrategy = getPatStrategy(PayType.ALIPAY);
//            return payStrategy.asyncNotify(request,payConfig);
//        }
//    }
//
//
//
//
//    public RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig){
//        PayType payType = getPayType(payConfig);
//        PayStrategy payStrategy = getPatStrategy(payType);
//        return payStrategy.asyncNotifyRefund(request,payConfig);
//    }
//
//    private PayType getPayType(PayConfig payConfig) {
//        PayType payType = null;
//        if(payConfig instanceof WxPayConfig){
//            payType = PayType.WX;
//        }
//        if(payConfig instanceof UnionPayConfig){
//            payType = PayType.UNION;
//        }
//        if(payConfig instanceof AliPayConfig){
//            payType = PayType.ALIPAY;
//        }
//        return payType;
//    }
//
//
//    public PayResponse asyncNotify(HttpServletRequest request){
//        return this.asyncNotify(request,null );
//    }
//
//
//
//    public PayResponse asyncNotify(HttpServletRequest request,PayConfig payConfig,PayType payType){
//        if (PayType.WX == payType) {
////          微信
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.WX);
//            }
//            PayStrategy payStrategy = getPatStrategy(PayType.WX);
//            return payStrategy.asyncNotify(request,payConfig);
//        } else  if(PayType.ALIPAY == payType){
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.ALIPAY);
//            }
//            PayStrategy payStrategy = getPatStrategy(PayType.ALIPAY);
//            return payStrategy.asyncNotify(request,payConfig);
//        }else if(PayType.UNION == payType){
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.UNION);
//            }
//            PayStrategy payStrategy = getPatStrategy(PayType.UNION);
//            return payStrategy.asyncNotify(request,payConfig);
//        }else{
//            return null;
//        }
//
//    }
//
//
//
//    public PayResponse query(OrderQueryRequest request, PayConfig payConfig){
//        PayType payType = request.getPayType();
//        if(request.getPayType() == null){
//            throw new PayException("未知的支付方式");
//        }
//        PayStrategy payStrategy = null;
//        if (PayType.WX == payType) {
////          微信
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.WX);
//            }
//            payStrategy = getPatStrategy(PayType.WX);
//        } else if(PayType.ALIPAY == payType){
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.ALIPAY);
//            }
//            payStrategy = getPatStrategy(PayType.ALIPAY);
//        }else if(PayType.UNION == payType){
//            if(payConfig == null){
//                payConfig = getDefaultPayConfig(PayType.UNION);
//            }
//            payStrategy = getPatStrategy(PayType.UNION);
//        }else{
//            throw new PayException("暂不支持该支付方式");
//        }
//        return payStrategy.query(request,payConfig);
//    }
//





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


    public static PayStrategy getPayStrategy(String wayCode) {
        return getPayStrategy(PayChannel.getByCode(wayCode));
    }

    public static PayStrategy getPayStrategy(PayChannel payChannel) {
        return getPayStrategy(payChannel.getPayType());
    }
    /**
     * 根据支付类型 和 配置获取具体的策略类
     * @param payType
     * @return
     */
    public static PayStrategy getPayStrategy(PayType payType) {
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
