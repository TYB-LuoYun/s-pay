package com.dicomclub.payment.module.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:39
 */
@Data
public abstract  class PayRequest extends AssistOrder{
    private PayChannel payChannel;


    private PayDataType payDataType;



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
     * 描述
     */
    private String orderDesc;

    /**
     * 附加内容，发起支付时传入,一些额外的信息，可以自定义格式，一般用于商户携带一些额外的业务信息，如用户的其他信息等。
     */
    private String attach;







}
