package com.dicomclub.module.pay.service;

import com.dicomclub.module.pay.config.PayConfig;
import com.dicomclub.module.pay.model.PayRequest;
import com.dicomclub.module.pay.model.PayResponse;

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
   public abstract PayResponse pay(PayRequest payRequest ,PayConfig payConfig);

   /**
    * 异步通知
    * @param notifyData
    * @return
    */
   public abstract PayResponse asyncNotify(String notifyData  ,PayConfig payConfig);


}
