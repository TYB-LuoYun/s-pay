package com.dicomclub.payment.module.pay.service;

import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.model.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:19
 */
public abstract class PayStrategy {
   /**
    * 订单预支付
    * @param payRequest
    * @return
    */
   public abstract PayResponse pay(PayRequest payRequest , PayConfig payConfig);

   /**
    * 异步通知
    * @param
    * @return
    */
   public abstract PayResponse asyncNotify(HttpServletRequest request, PayConfig payConfig);


    /**
     * 异步通知-退款
     * 与支付结果不同的是，退款结果可能会受到更多的影响因素，比如退款金额、退款原因、退款渠道等，因此退款处理的时间可能会比支付处理更长。通过异步回调接口，商户可以及时获取退款结果，以便及时处理相关业务。
     * @param
     * @return
     */
    public abstract RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig);


   /**
    * 订单结果查询
    */
   public abstract PayResponse query(OrderQueryRequest request, PayConfig payConfig);


   /**
    * 退款
    */
   public abstract RefundResponse refund(RefundRequest request, PayConfig payConfig);


    /**
     * 退款查询
     */
    public abstract RefundResponse refundQuery(RefundQueryRequest refundNo, PayConfig payConfig);


    /**
     * 转账
     */
    public abstract TransferResponse transfer(TransferOrder order,PayConfig payConfig);




}
