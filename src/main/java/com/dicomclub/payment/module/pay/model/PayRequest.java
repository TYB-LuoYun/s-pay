package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:39
 */
@Data
public abstract  class PayRequest {
    private PayChannel payChannel;


    private PayDataType payDataType;

    /**
     * 订单号.
     */
    private String orderNo;

    /**
     * 订单金额.
     */
    private BigDecimal orderAmount;

    /**
     * 订单名字.
     */
    private String orderName;


    /**
     * 付款码
     */
    private String authCode;

    /**
     * 附加内容，发起支付时传入
     */
    private String attach;


}
